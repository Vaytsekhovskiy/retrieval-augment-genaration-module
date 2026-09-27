package org.vced.ragutillib.orm;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.util.Collection;

@Setter
@Getter
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class AuthenticationDataORM {
    private String name;
    private Collection<String> authorities;
}