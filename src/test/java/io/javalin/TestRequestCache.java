package io.javalin;

import com.mashape.unirest.http.HttpResponse;
import io.javalin.newutil.TestUtil;
import java.io.InputStream;
import java.util.Arrays;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.Test;
import static org.hamcrest.MatcherAssert.assertThat;

public class TestRequestCache {

    @Test
    public void test_cache_not_draining_InputStream() {
        new TestUtil().test((app, http) -> {
            app.post("/cache-chunked-encoding", ctx -> ctx.result(ctx.req.getInputStream()));
            byte[] body = new byte[10000];
            for (int i = 0; i < body.length; i++) {
                body[i] = (byte) (i % 256);
            }
            CloseableHttpClient client = HttpClients.createDefault();
            HttpPost post = new HttpPost(http.origin + "/cache-chunked-encoding");
            ByteArrayEntity entity = new ByteArrayEntity(body);
            entity.setChunked(true);
            post.setEntity(entity);
            CloseableHttpResponse response = client.execute(post);
            byte[] result = EntityUtils.toByteArray(response.getEntity());
            assertThat("Body should match", Arrays.equals(result, body));
            response.close();
        });
    }

    @Test
    public void test_allows_disabling_cache() {
        new TestUtil(Javalin.create().disableRequestCache()).test((app, http) -> {
            app.post("/disabled-cache", ctx -> {
                if (ctx.req.getInputStream().getClass().getSimpleName().equals("CachedServletInputStream")) {
                    throw new IllegalStateException("Cache should be disabled");
                } else {
                    ctx.result("");
                }
            });
            HttpResponse<InputStream> response = http.post("/disabled-cache")
                .body("test")
                .asBinary();
            assertThat("Request cache should be disabled", response.getStatus() == 200);
        });
    }
}
