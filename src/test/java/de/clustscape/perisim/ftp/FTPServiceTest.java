package de.clustscape.perisim.ftp;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Appender;
import de.clustscape.perisim.service.ftp.FTPService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.LoggerFactory;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Random;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

/**
 * Tests the FTPService.
 *
 * @author Marco Werner
 */
@RunWith(MockitoJUnitRunner.class)
public class FTPServiceTest {

    @Mock
    private Appender<ILoggingEvent> mockAppender;

    @Captor
    private ArgumentCaptor<LoggingEvent> captorLoggingEvent;

    @InjectMocks
    private FTPService ftpService;

    @Before
    public void setup() {
        Logger logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
        logger.addAppender(mockAppender);
    }

    @Test
    public void testStartupDisabled() {
        // given

        // when
        ftpService.startup();

        // then
        assertThat(ftpService.isRunning(), is(false));
        verify(mockAppender, atLeastOnce()).doAppend(captorLoggingEvent.capture());
        List<LoggingEvent> loggingEvents = captorLoggingEvent.getAllValues();
        assertTrue(loggingEvents.stream().anyMatch(loggingEvent -> loggingEvent.getFormattedMessage().contains("disabled")));
    }

    @Test
    public void testStartupEnabled() {
        // given
        ReflectionTestUtils.setField(ftpService, "enabled", true);
        ReflectionTestUtils.setField(ftpService, "port", new Random().nextInt(64512) + 1024);
        ReflectionTestUtils.setField(ftpService, "usersFile", "ftpservice/ftpservice_users_test.properties");

        // when
        ftpService.startup();

        // then
        assertThat(ftpService.isRunning(), is(true));
        verify(mockAppender, atLeastOnce()).doAppend(captorLoggingEvent.capture());
        List<LoggingEvent> loggingEvents = captorLoggingEvent.getAllValues();
        assertTrue(loggingEvents.stream().anyMatch(loggingEvent -> loggingEvent.getFormattedMessage().contains("SSL enabled: false")));
    }

    @Test
    public void testStartupSsl() {
        // given
        ReflectionTestUtils.setField(ftpService, "enabled", true);
        ReflectionTestUtils.setField(ftpService, "port", new Random().nextInt(64512) + 1024);
        ReflectionTestUtils.setField(ftpService, "usersFile", "ftpservice/ftpservice_users_test.properties");
        ReflectionTestUtils.setField(ftpService, "sslEnabled", true);
        ReflectionTestUtils.setField(ftpService, "keystoreFile", "ftpservice/ftpservice_test.jks");
        ReflectionTestUtils.setField(ftpService, "keystorePassword", "secret");

        // when
        ftpService.startup();

        // then
        assertThat(ftpService.isRunning(), is(true));
        verify(mockAppender, atLeastOnce()).doAppend(captorLoggingEvent.capture());
        List<LoggingEvent> loggingEvents = captorLoggingEvent.getAllValues();
        assertTrue(loggingEvents.stream().anyMatch(loggingEvent -> loggingEvent.getFormattedMessage().contains("SSL enabled: true")));
    }

    @Test
    public void testShutdown() {
        // given
        ReflectionTestUtils.setField(ftpService, "enabled", true);
        ReflectionTestUtils.setField(ftpService, "port", new Random().nextInt(64512) + 1024);
        ReflectionTestUtils.setField(ftpService, "usersFile", "ftpservice/ftpservice_users_test.properties");
        ftpService.startup();
        assertThat(ftpService.isRunning(), is(true));

        // when
        ftpService.shutdown();

        // then
        assertThat(ftpService.isRunning(), is(false));
        verify(mockAppender, atLeastOnce()).doAppend(captorLoggingEvent.capture());
        List<LoggingEvent> loggingEvents = captorLoggingEvent.getAllValues();
        assertTrue(loggingEvents.stream().anyMatch(loggingEvent -> loggingEvent.getFormattedMessage().contains("is down")));
    }

    @Test
    public void testShutdownDisabled() {
        // given
        ReflectionTestUtils.setField(ftpService, "enabled", false);
        ReflectionTestUtils.setField(ftpService, "usersFile", "ftpservice/ftpservice_users_test.properties");
        ftpService.startup();
        assertThat(ftpService.isRunning(), is(false));

        // when
        ftpService.shutdown();

        // then
        assertThat(ftpService.isRunning(), is(false));
        verify(mockAppender, atLeastOnce()).doAppend(captorLoggingEvent.capture());
        List<LoggingEvent> loggingEvents = captorLoggingEvent.getAllValues();
        assertTrue(loggingEvents.stream().noneMatch(loggingEvent -> loggingEvent.getFormattedMessage().contains("is down")));
    }
}
