package A7.core;

import A7.utils.MsgWrapper;

import com.google.common.cache.CacheBuilder;
import com.google.protobuf.ByteString;
import com.google.common.cache.Cache;
import java.util.concurrent.TimeUnit;

public class RequestCache {
	private static RequestCache instance = new RequestCache();
    private Cache<ByteString, MsgWrapper> requestCache;

    // TODO: Implement cache exceptions, size limits
    private RequestCache() {
        requestCache = CacheBuilder.newBuilder()
            .maximumSize(500)
            .expireAfterWrite(5, TimeUnit.SECONDS)
            .build();
    }

    public static RequestCache getInstance() {
        return instance;
    }

    public Cache<ByteString, MsgWrapper> getCache() {
        return requestCache;
    }
}
