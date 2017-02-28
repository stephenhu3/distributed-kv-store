package A4.server;

import A4.proto.Message.Msg;
import A4.utils.MsgWrapper;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.util.concurrent.TimeUnit;

public class RequestCache {
    private static RequestCache instance = new RequestCache();
    LoadingCache<Msg, MsgWrapper> requestCache;

    // TODO: Implement cache exceptions, size limits
    private RequestCache() {
        requestCache = CacheBuilder.newBuilder()
            .maximumSize(500)
            .expireAfterWrite(60, TimeUnit.SECONDS)
            .build(
                new CacheLoader<Msg, MsgWrapper>() {
                    public MsgWrapper load(Msg key) throws Exception {
                        KVRequestQueue.getInstance().getQueue().add(key);
                        // wait until KVResponseQueue has processed
                        while (KVResponseQueue.getInstance().getQueue().isEmpty());
                        MsgWrapper msgWrapper = ForwardingQueue.getInstance().getQueue().poll();
                        msgWrapper.setMessage(KVResponseQueue.getInstance().getQueue().poll());
                        return msgWrapper;
                    }
                }
            );
    }

    public static RequestCache getInstance() {
        return instance;
    }

    public LoadingCache<Msg, MsgWrapper> getCache() {
        return requestCache;
    }
}
