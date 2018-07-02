/*
 * Javalin - https://javalin.io
 * Copyright 2017 David Åse
 * Licensed under Apache 2.0: https://github.com/tipsy/javalin/blob/master/LICENSE
 */

package io.javalin

import io.javalin.core.util.Util
import io.javalin.util.TestUtil
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.containsString
import org.junit.Test
import java.util.function.BiFunction
import java.util.function.Function

class TestContextPath {

    @Test
    fun test_normalizeContextPath_works() {
        val normalize = Function<String, String> { Util.normalizeContextPath(it) }
        assertThat(normalize.apply("path"), `is`("/path"))
        assertThat(normalize.apply("/path"), `is`("/path"))
        assertThat(normalize.apply("/path/"), `is`("/path"))
        assertThat(normalize.apply("//path/"), `is`("/path"))
        assertThat(normalize.apply("/path//"), `is`("/path"))
        assertThat(normalize.apply("////path////"), `is`("/path"))
    }

    @Test
    fun test_prefixPath_works() {
        val prefix = BiFunction<String, String, String> { contextPath, path -> Util.prefixContextPath(contextPath, path) }
        assertThat(prefix.apply("/c-p", "*"), `is`("*"))
        assertThat(prefix.apply("/c-p", "/*"), `is`("/c-p/*"))
        assertThat(prefix.apply("/c-p", "path"), `is`("/c-p/path"))
        assertThat(prefix.apply("/c-p", "/path"), `is`("/c-p/path"))
        assertThat(prefix.apply("/c-p", "//path"), `is`("/c-p/path"))
        assertThat(prefix.apply("/c-p", "/path/"), `is`("/c-p/path/"))
        assertThat(prefix.apply("/c-p", "//path//"), `is`("/c-p/path/"))
    }

    @Test
    fun test_router_works() = TestUtil(Javalin.create().contextPath("/context-path")).test { app, http ->
        app.get("/hello") { ctx -> ctx.result("Hello World") }
        assertThat(http.getBody("/hello"), `is`("Not found. Request is below context-path (context-path: '/context-path')"))
        assertThat(http.getBody("/context-path/hello"), `is`("Hello World"))
    }

    @Test
    fun test_twoLevelContextPath_works() = TestUtil(Javalin.create().contextPath("/context-path/path-context")).test { app, http ->
        app.get("/hello") { ctx -> ctx.result("Hello World") }
        assertThat(http.get("/context-path/").code(), `is`(404))
        assertThat(http.getBody("/context-path/path-context/hello"), `is`("Hello World"))
    }

    @Test
    fun test_staticFiles_work() = TestUtil(Javalin.create().contextPath("/context-path").enableStaticFiles("/public")).test { app, http ->
        assertThat(http.get("/script.js").code(), `is`(404))
        assertThat(http.getBody("/context-path/script.js"), containsString("JavaScript works"))
    }

    @Test
    fun test_welcomeFile_works() = TestUtil(Javalin.create().contextPath("/context-path").enableStaticFiles("/public")).test { app, http ->
        assertThat(http.getBody("/context-path/subdir/"), `is`("<h1>Welcome file</h1>"))
    }

}
