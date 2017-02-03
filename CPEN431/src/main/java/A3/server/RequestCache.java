package A3.server;

import A3.proto.Message.Msg;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.util.concurrent.TimeUnit;

public class RequestCache {
    private static RequestCache instance = new RequestCache();
    LoadingCache<Msg, Msg> requestCache;

    // TODO: Implement cache exceptions, size limits
    private RequestCache() {
        requestCache = CacheBuilder.newBuilder()
            .maximumSize(500)
            .expireAfterWrite(60, TimeUnit.SECONDS)
            .build(
                new CacheLoader<Msg, Msg>() {
                    public Msg load(Msg key) throws Exception {
                        KVRequestQueue.getInstance().getQueue().add(key);
                        // wait until KVResponseQueue has processed
                        while (KVResponseQueue.getInstance().getQueue().isEmpty());
                        return  KVResponseQueue.getInstance().getQueue().poll();
                    }
                }
            );
    }

    public static RequestCache getInstance() {
        return instance;
    }

    public LoadingCache<Msg, Msg> getCache() {
        return requestCache;
    }
}
