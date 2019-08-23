package ch.loewenfels.issuetrackingsync.controller

import ch.loewenfels.issuetrackingsync.AbstractSpringTest
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

internal class StatisticsControllerTest : AbstractSpringTest() {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    @WithMockUser(roles = ["USER"])
    fun whenCalled_shouldReturnHello() {
        mockMvc.perform(
            get("/statistics")
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk())
    }
}