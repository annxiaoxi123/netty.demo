package own.lty.netty.demo.handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class PrintStringHandler extends ChannelInboundHandlerAdapter {

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object message) {
		String msgStr = (String)message;
		System.out.println(msgStr);
		if(msgStr.startsWith("close")){
			System.out.println("close channel");
			ctx.channel().close();
		}else{
			ctx.channel().writeAndFlush("receive msg:"+msgStr);
		}
	}

}
