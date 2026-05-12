package com.onemoretechie.sample.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ItemControllerTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void createListGetUpdateDelete_happyPath() throws Exception {
        // create
        String created = mvc.perform(post("/api/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"widget\",\"quantity\":3}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("widget"))
                .andExpect(jsonPath("$.quantity").value(3))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long id = extractId(created);

        // list contains it
        mvc.perform(get("/api/items"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.name=='widget')]").exists());

        // get one
        mvc.perform(get("/api/items/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(3));

        // update
        mvc.perform(put("/api/items/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"widget\",\"quantity\":7}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(7));

        // delete
        mvc.perform(delete("/api/items/" + id))
                .andExpect(status().isNoContent());

        // and now 404
        mvc.perform(get("/api/items/" + id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("not_found"));
    }

    @Test
    void create_rejectsInvalidPayload() throws Exception {
        mvc.perform(post("/api/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\",\"quantity\":-1}"))
                .andExpect(status().isBadRequest());
    }

    private static Long extractId(String json) {
        // tiny parser — body is "{\"id\":N,...}"
        int start = json.indexOf("\"id\":") + 5;
        int end = json.indexOf(',', start);
        return Long.parseLong(json.substring(start, end).trim());
    }
}
