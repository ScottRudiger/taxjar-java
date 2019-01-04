package com.taxjar.exception;

import com.taxjar.MockInterceptor;
import com.taxjar.Taxjar;
import com.taxjar.TaxjarMock;
import com.taxjar.model.categories.CategoryResponse;
import com.taxjar.net.Listener;
import junit.framework.TestCase;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.MockResponse;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class ExceptionTest extends TestCase {
    public void testException() {
        TaxjarException e = null;

        Map<String, Object> opts = new HashMap<>();
        opts.put("code", 400);
        MockInterceptor interceptor = new MockInterceptor("error.json", opts);
        TaxjarMock client = new TaxjarMock("TEST", interceptor);

        try {
            CategoryResponse res = client.categories();
        } catch (TaxjarException ex) {
            e = ex;
        }

        assertTrue(e instanceof TaxjarException);
        assertEquals("Bad Request - Your request format is bad.", e.getMessage());
        assertEquals((Integer) 400, e.getStatusCode());
    }

    public void testExceptionAsync() throws InterruptedException {
        Map<String, Object> opts = new HashMap<>();
        opts.put("code", 400);
        MockInterceptor interceptor = new MockInterceptor("error.json", opts);
        TaxjarMock client = new TaxjarMock("TEST", interceptor);

        client.categories(new Listener<CategoryResponse>() {
            @Override
            public void onSuccess(CategoryResponse res)
            {
                fail("Call was successful");
            }

            @Override
            public void onError(TaxjarException e)
            {
                assertTrue(e instanceof TaxjarException);
                assertEquals("Bad Request - Your request format is bad.", e.getMessage());
                assertEquals((Integer) 400, e.getStatusCode());
            }
        });

        Thread.sleep(1000);
    }

    public void testTimeoutException() throws IOException {
        TaxjarException e = null;
        MockWebServer server = new MockWebServer();

        server.enqueue(new MockResponse().setBody("{}").setBodyDelay(800, TimeUnit.MILLISECONDS));
        server.start();

        Map<String, Object> params = new HashMap<>();
        params.put("apiUrl", "http://" + server.getHostName() + ":" + server.getPort());
        params.put("timeout", 500);
        Taxjar client = new Taxjar("TEST", params);

        try {
            CategoryResponse res = client.categories();
        } catch (TaxjarException ex) {
            e = ex;
        }

        assertTrue(e instanceof TaxjarException);
        assertEquals("timeout", e.getMessage());

        server.shutdown();
    }

    public void testTimeoutExceptionAsync() throws IOException, InterruptedException {
        MockWebServer server = new MockWebServer();

        server.enqueue(new MockResponse().setBody("{}").setBodyDelay(800, TimeUnit.MILLISECONDS));
        server.start();

        Map<String, Object> params = new HashMap<>();
        params.put("apiUrl", "http://" + server.getHostName() + ":" + server.getPort());
        params.put("timeout", 500);
        Taxjar client = new Taxjar("TEST", params);

        client.categories(new Listener<CategoryResponse>() {
            @Override
            public void onSuccess(CategoryResponse res)
            {
                fail("Call was successful");
            }

            @Override
            public void onError(TaxjarException e)
            {
                assertTrue(e instanceof TaxjarException);
                assertEquals("timeout", e.getMessage());
            }
        });

        Thread.sleep(1000);

        server.shutdown();
    }
}
