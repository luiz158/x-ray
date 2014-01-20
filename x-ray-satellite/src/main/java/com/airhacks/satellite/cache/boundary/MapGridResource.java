/*
 */
package com.airhacks.satellite.cache.boundary;

import com.hazelcast.core.IMap;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonWriter;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

/**
 *
 * @author adam-bien.com
 */
public class MapGridResource implements GridResource {

    IMap<String, String> cache;

    public MapGridResource(IMap<String, String> cache) {
        this.cache = cache;
    }

    @GET
    public JsonObject all(@DefaultValue("100") @QueryParam("max") int max) {
        JsonObjectBuilder builder = Json.createObjectBuilder();
        cache.entrySet().stream().limit(max).forEach((entry) -> {
            String key = entry.getKey();
            String value = entry.getValue();
            builder.add(key, value);
        });
        return builder.build();
    }

    @PUT
    @Path("{key}")
    public void put(@PathParam("key") String key, JsonObject jsonObject) throws IOException {
        try (final StringWriter stringWriter = new StringWriter()) {
            JsonWriter writer = Json.createWriter(stringWriter);
            writer.writeObject(jsonObject);
            stringWriter.flush();
            cache.putAsync(key, stringWriter.getBuffer().toString());
        }
    }

    @GET
    @Path("{key}")
    public Response find(@PathParam("key") String key) {
        String serialized = this.cache.get(key);
        if (serialized == null) {
            return Response.noContent().build();
        }
        JsonObject retVal = Json.createReader(new StringReader(serialized)).readObject();
        return Response.ok(retVal).build();
    }

    @DELETE
    @Path("{key}")
    @Override
    public void delete(@PathParam("key") String key) {
        this.cache.removeAsync(key);
    }
}
