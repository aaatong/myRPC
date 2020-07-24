## 2020/07/24

1. consumer发送请求后，provider没有收到，consumer本地也没有报错
    - 原因：没有捕获netty出站过程的异常
    - 解决：netty入站过程的异常处理可以在handler中的exceptionCaught方法中进行，而出站过程中的异常要在出站操作（write等）返回的ChannelFuture上的Listener中进行处理
    
2. kryo序列化出错
    - 原因：kryo默认支持的可序列化类型不包括Class，Class[]，Object[]类型
    - 解决：手动注册这些类型
    
3. Consumer端的出站流程中，ByteBuf的引用计数在被最终释放前变为0，但代码中并没有显式释放的操作
    - 原因：在encode方法中调用了writeAndFlush方法，该方法会导致netty释放ByteBuf
    - 解决：删除writeAndFlush方法