package com.example.fx.service;

import com.example.fx.model.ExchangeRequest;
import com.example.fx.model.ExchangeResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.mock.web.MockMultipartFile;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FileServiceTest {

    @Mock
    private ExchangeService exchangeService;

    @InjectMocks
    private FileService fileService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        fileService = new FileService(exchangeService);
    }

    @Test
    void testHandleValidCsv() {
        String csvContent = "amount,from,to\n100,USD,EUR\n200,GBP,USD";
        MockMultipartFile file = new MockMultipartFile("file", "test.csv", "text/csv", csvContent.getBytes());

        when(exchangeService.convert(any(ExchangeRequest.class))).thenReturn(
                new ExchangeResponse(90, java.util.UUID.randomUUID()));

        List<ExchangeResponse> results = fileService.handle(file);

        assertEquals(2, results.size());
        verify(exchangeService, times(2)).convert(any(ExchangeRequest.class));
    }

    @Test
    void testHandleInvalidCsvThrows() {
        String badCsv = "amount,from,to\ninvalid_line_without_enough_parts";
        MockMultipartFile file = new MockMultipartFile("file", "bad.csv", "text/csv", badCsv.getBytes());

        RuntimeException thrown = assertThrows(RuntimeException.class, () -> fileService.handle(file));
        assertNotNull("File processing error", thrown.getMessage());
    }


}