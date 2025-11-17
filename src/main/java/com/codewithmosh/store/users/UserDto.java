package com.codewithmosh.store.users;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class UserDto {
    //@JsonIgnore // per fare in modo che venga ignorato l id in ritorno (come se lo togliessi)
    //@JsonProperty("user_id") //per rinominarlo invece
    private Long id;
    private String name;
    private String email;
    //@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    //private LocalDateTime createdAt; // campo aggiuntivo che ritornato è null dato che non lo abbiamo in User
}
