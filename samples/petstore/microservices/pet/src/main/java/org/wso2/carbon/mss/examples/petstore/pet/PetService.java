/*
 *  Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.mss.examples.petstore.pet;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.mss.examples.petstore.util.JedisUtil;
import org.wso2.carbon.mss.examples.petstore.util.model.Pet;

import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

/**
 * Pet microservice
 */
@Path("/pet")
public class PetService {
    private static final Logger log = LoggerFactory.getLogger(PetService.class);
    private static final String PET_PREFIX = "$PET_";

    static {
        log.info("SENTINEL1_HOST: {}", System.getenv("SENTINEL1_HOST"));
        log.info("SENTINEL1_PORT: {}", System.getenv("SENTI" +
                "NEL1_PORT"));
    }

    @POST
    @Consumes("application/json")
    public Response addPet(Pet pet) {
        String id = pet.getId();
        if (JedisUtil.get(PET_PREFIX + id) != null) {
            return Response.status(Response.Status.CONFLICT).
                    entity("Pet with ID " + id + " already exists").build();
        } else {
            JedisUtil.set(PET_PREFIX + id, new Gson().toJson(pet));
            log.info("Added pet");
        }
        return Response.status(Response.Status.OK).entity("Pet with ID " + id + " successfully added").build();
    }

    @DELETE
    @Path("/{id}")
    public Response deletePet(@PathParam("id") String id) {
        String json = JedisUtil.get(PET_PREFIX + id);
        if (json == null || json.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        JedisUtil.del(PET_PREFIX + id);
        log.info("Deleted pet");
        return Response.status(Response.Status.OK).entity("OK").build();
    }

    @PUT
    @Consumes("application/json")
    public Response updatePet(Pet pet) {
        String id = pet.getId();
        String json = JedisUtil.get(PET_PREFIX + id);
        if (json == null || json.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        } else {
            JedisUtil.set(PET_PREFIX + id, new Gson().toJson(pet));
            log.info("Updated pet");
            return Response.status(Response.Status.OK).entity("Pet with ID " + id + " successfully updated").build();
        }
    }

    @GET
    @Produces("application/json")
    @Path("/{id}")
    public Response getPet(@PathParam("id") String id) {
        String json = JedisUtil.get(PET_PREFIX + id);
        if (json == null || json.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        log.info("Got pet");
        return Response.status(Response.Status.OK).entity(new Gson().fromJson(json, Pet.class)).build();
    }

    @GET
    @Path("/all")
    public List<Pet> getAllCategories() {
        List<String> values = JedisUtil.getValues(PET_PREFIX + "*");
        List<Pet> result = new ArrayList<>();
        for (String value : values) {
            result.add(new Gson().fromJson(value, Pet.class));
        }
        return result;
    }
}
