package com.mktekhub.inventory.controller;

import com.mktekhub.inventory.dto.StockActivityResponse;
import com.mktekhub.inventory.model.ActivityType;
import com.mktekhub.inventory.service.StockActivityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class StockActivityControllerTest {

    @Mock
    private StockActivityService stockActivityService;

    @InjectMocks
    private StockActivityController stockActivityController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(stockActivityController).build();
    }

    @Test
    void testGetAllActivities() throws Exception {
        // Arrange
        StockActivityResponse activity1 = new StockActivityResponse();
        activity1.setId(1L);
        activity1.setActivityType(ActivityType.RECEIVE);

        List<StockActivityResponse> activities = Arrays.asList(activity1);
        when(stockActivityService.getAllActivities()).thenReturn(activities);

        // Act & Assert
        mockMvc.perform(get("/api/stock-activities"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));

        verify(stockActivityService, times(1)).getAllActivities();
    }

    @Test
    void testGetActivityById() throws Exception {
        // Arrange
        StockActivityResponse activity = new StockActivityResponse();
        activity.setId(1L);
        activity.setActivityType(ActivityType.RECEIVE);

        when(stockActivityService.getActivityById(1L)).thenReturn(activity);

        // Act & Assert
        mockMvc.perform(get("/api/stock-activities/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(stockActivityService, times(1)).getActivityById(1L);
    }

    @Test
    void testGetActivitiesByItemId() throws Exception {
        // Arrange
        List<StockActivityResponse> activities = Arrays.asList(new StockActivityResponse());
        when(stockActivityService.getActivitiesByItemIdSorted(1L)).thenReturn(activities);

        // Act & Assert
        mockMvc.perform(get("/api/stock-activities/item/1"))
                .andExpect(status().isOk());

        verify(stockActivityService, times(1)).getActivitiesByItemIdSorted(1L);
    }

    @Test
    void testGetActivitiesByItemSku() throws Exception {
        // Arrange
        List<StockActivityResponse> activities = Arrays.asList(new StockActivityResponse());
        when(stockActivityService.getActivitiesByItemSku("SKU001")).thenReturn(activities);

        // Act & Assert
        mockMvc.perform(get("/api/stock-activities/sku/SKU001"))
                .andExpect(status().isOk());

        verify(stockActivityService, times(1)).getActivitiesByItemSku("SKU001");
    }

    @Test
    void testGetActivitiesByType() throws Exception {
        // Arrange
        List<StockActivityResponse> activities = Arrays.asList(new StockActivityResponse());
        when(stockActivityService.getActivitiesByType(ActivityType.RECEIVE)).thenReturn(activities);

        // Act & Assert
        mockMvc.perform(get("/api/stock-activities/type/RECEIVE"))
                .andExpect(status().isOk());

        verify(stockActivityService, times(1)).getActivitiesByType(ActivityType.RECEIVE);
    }

    @Test
    void testGetActivitiesByUserId() throws Exception {
        // Arrange
        List<StockActivityResponse> activities = Arrays.asList(new StockActivityResponse());
        when(stockActivityService.getActivitiesByUserId(1L)).thenReturn(activities);

        // Act & Assert
        mockMvc.perform(get("/api/stock-activities/user/1"))
                .andExpect(status().isOk());

        verify(stockActivityService, times(1)).getActivitiesByUserId(1L);
    }

    @Test
    void testGetActivitiesByUsername() throws Exception {
        // Arrange
        List<StockActivityResponse> activities = Arrays.asList(new StockActivityResponse());
        when(stockActivityService.getActivitiesByUsername("testuser")).thenReturn(activities);

        // Act & Assert
        mockMvc.perform(get("/api/stock-activities/user/username/testuser"))
                .andExpect(status().isOk());

        verify(stockActivityService, times(1)).getActivitiesByUsername("testuser");
    }

    @Test
    void testGetActivitiesByWarehouse() throws Exception {
        // Arrange
        List<StockActivityResponse> activities = Arrays.asList(new StockActivityResponse());
        when(stockActivityService.getActivitiesByWarehouse(1L)).thenReturn(activities);

        // Act & Assert
        mockMvc.perform(get("/api/stock-activities/warehouse/1"))
                .andExpect(status().isOk());

        verify(stockActivityService, times(1)).getActivitiesByWarehouse(1L);
    }

    @Test
    void testGetActivitiesBySourceWarehouse() throws Exception {
        // Arrange
        List<StockActivityResponse> activities = Arrays.asList(new StockActivityResponse());
        when(stockActivityService.getActivitiesBySourceWarehouse(1L)).thenReturn(activities);

        // Act & Assert
        mockMvc.perform(get("/api/stock-activities/warehouse/source/1"))
                .andExpect(status().isOk());

        verify(stockActivityService, times(1)).getActivitiesBySourceWarehouse(1L);
    }

    @Test
    void testGetActivitiesByDestinationWarehouse() throws Exception {
        // Arrange
        List<StockActivityResponse> activities = Arrays.asList(new StockActivityResponse());
        when(stockActivityService.getActivitiesByDestinationWarehouse(1L)).thenReturn(activities);

        // Act & Assert
        mockMvc.perform(get("/api/stock-activities/warehouse/destination/1"))
                .andExpect(status().isOk());

        verify(stockActivityService, times(1)).getActivitiesByDestinationWarehouse(1L);
    }

    @Test
    void testGetActivitiesByDateRange() throws Exception {
        // Arrange
        LocalDateTime start = LocalDateTime.of(2024, 1, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2024, 12, 31, 23, 59);

        List<StockActivityResponse> activities = Arrays.asList(new StockActivityResponse());
        when(stockActivityService.getActivitiesByDateRange(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(activities);

        // Act & Assert
        mockMvc.perform(get("/api/stock-activities/date-range")
                .param("startDate", "2024-01-01T00:00:00")
                .param("endDate", "2024-12-31T23:59:59"))
                .andExpect(status().isOk());

        verify(stockActivityService, times(1)).getActivitiesByDateRange(any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @Test
    void testGetActivitiesWithFilters() throws Exception {
        // Arrange
        List<StockActivityResponse> activities = Arrays.asList(new StockActivityResponse());
        when(stockActivityService.getActivitiesWithFilters(any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(activities);

        // Act & Assert
        mockMvc.perform(get("/api/stock-activities/filter")
                .param("itemId", "1")
                .param("activityType", "RECEIVE"))
                .andExpect(status().isOk());

        verify(stockActivityService, times(1))
                .getActivitiesWithFilters(any(), any(), any(), any(), any(), any(), any(), any());
    }
}
