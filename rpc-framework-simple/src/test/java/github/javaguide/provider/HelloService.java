package github.javaguide.provider;

import github.javaguide.annotation.RpcService;

/**
 * @author zhp
 * @date 2022-10-26 12:19
 */
@RpcService(version = "5.0",group = "test")
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
