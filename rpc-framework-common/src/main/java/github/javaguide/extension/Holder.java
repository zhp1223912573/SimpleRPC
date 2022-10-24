package github.javaguide.extension;

/**
 * @author zhp
 * @date 2022-10-24 16:18
 * 持有者
 */
public class Holder<T> {
    private volatile T value;

    public T getValue(){
        return value;
    }

    public void setValue(T value){
        this.value = value;
    }
}
