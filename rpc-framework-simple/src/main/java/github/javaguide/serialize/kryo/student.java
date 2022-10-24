package github.javaguide.serialize.kryo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author zhp
 * @date 2022-10-24 22:33
 */
@AllArgsConstructor
@Getter
@Setter
@ToString
public class student {
    private String name;
    private String gender;

    public student() {
    }
}
