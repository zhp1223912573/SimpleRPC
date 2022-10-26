package github.javaguide.provider;

/**
 * @author zhp
 * @date 2022-10-26 12:19
 */
public class HelloService implements service {

    public void sayHello(){
        System.out.println("你好！");
    }

    @Override
    public String doSomething(String msg) {
        sayHello();
        System.out.println(""+msg);
        return "好的，你是涡蝶";
    }
}
