package github.javaguide.serialize.kryo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

/**
 * @author zhp
 * @date 2022-10-24 22:33
 */
@AllArgsConstructor
@Getter
@Setter
@ToString
public class student implements Serializable {

    private String name;
    private String gender;

    public student() {
    }
}
