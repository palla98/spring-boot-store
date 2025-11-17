package com.codewithmosh.store.users;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@AllArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public List<UserDto> findAll(String sort){
        //validazione del request parameter:
        //1. ci assicuriamo che possa essere opzionale con il required = false
        //2. vogliamo che come default quindi in caso non passo niente sia una stringa vuota sennò nel confronto con il set da eccezione
        //3. e vogliamo che il nome della key in caso cambi da key a sort e uno usa postman sia sicuro che continui a funzionare come "sort"
        //4. infine mi assicuro che il valore che passo sia o name o email altrimenti setta name
        if (!Set.of("name", "email").contains(sort))
            sort = "name";
        return userRepository.findAll(Sort.by(Sort.Direction.DESC, sort))
                .stream()
                .map(userMapper::toDto)
                .toList();
    }

    public UserDto findById(Long id){
        var user = userRepository.findById(id).orElseThrow(UserNotFoundException::new);
        return userMapper.toDto(user);
    }

    public UserDto create(RegisterUserRequest request){
        //grazie a JPABuddy in automatico mi crea il metodo getEmail() nello user repository
        if(userRepository.existsByEmail(request.getEmail())){
            throw new UserAlreadyExists();
        }
        // 2. DESERIALIZZAZIONE (Mapping Request -> Entity)
        // MapStruct converte il DTO in Entità 'User'.
        var user = userMapper.toEntity(request);

        //salvo l hash delle password e non in clearText
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        //setto il ruolo dello user
        user.setRole(Role.USER);

        // userRepository.save() salva l'entità nel database.
        // L'entità 'user' ora contiene l'ID generato dal database.
        userRepository.save(user);

        // 3. SERIALIZZAZIONE (Mapping Entity -> Response DTO)
        // Converte l'entità salvata nel DTO di risposta (UserDto),
        // che NON contiene la password (sicurezza).
        return userMapper.toDto(user);
    }

    public UserDto update(UpdateUserRequest request, Long id){
        var user = userRepository.findById(id).orElseThrow(UserNotFoundException::new);
        // update: dentro l interfaccia mapper c'è il contratto del metodo che poi verrà implementato da spring
        userMapper.update(request, user);
        userRepository.save(user);

        return userMapper.toDto(user);
    }

    public UserDto delete(Long id) {
        var user = userRepository.findById(id).orElseThrow(UserNotFoundException::new);
        userRepository.delete(user);

        return userMapper.toDto(user);
    }

    public void changePassword(Long id, ChangePasswordRequest request) {
        var user = userRepository.findById(id).orElseThrow(UserNotFoundException::new);
        if (!user.getPassword().equals(request.getOldPassword())) {
            throw new AccessDeniedException("Passwords don't match");
        }
        user.setPassword(request.getNewPassword());
        userRepository.save(user);
    }
}
