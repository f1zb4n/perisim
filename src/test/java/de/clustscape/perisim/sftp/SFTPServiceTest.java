package de.clustscape.perisim.sftp;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Appender;
import de.clustscape.perisim.service.sftp.SFTPService;
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
 * Tests the SFTPService.
 *
 * @author Marco Werner
 */
@RunWith(MockitoJUnitRunner.class)
public class SFTPServiceTest {

    @Mock
    private Appender<ILoggingEvent> mockAppender;

    @Captor
    private ArgumentCaptor<LoggingEvent> captorLoggingEvent;

    @InjectMocks
    private SFTPService sftpService;

    @Before
    public void setup() {
        Logger logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
        logger.addAppender(mockAppender);
    }

    @Test
    public void testStartupDisabled() {
        // given

        // when
        sftpService.startup();

        // then
        assertThat(sftpService.isRunning(), is(false));
        verify(mockAppender, atLeastOnce()).doAppend(captorLoggingEvent.capture());
        List<LoggingEvent> loggingEvents = captorLoggingEvent.getAllValues();
        assertTrue(loggingEvents.stream().anyMatch(loggingEvent -> loggingEvent.getFormattedMessage().contains("disabled")));
    }

    @Test
    public void testStartupEnabled() {
        // given
        ReflectionTestUtils.setField(sftpService, "enabled", true);
        ReflectionTestUtils.setField(sftpService, "port", new Random().nextInt(64512) + 1024);

        // when
        sftpService.startup();

        // then
        assertThat(sftpService.isRunning(), is(true));
        verify(mockAppender, atLeastOnce()).doAppend(captorLoggingEvent.capture());
        List<LoggingEvent> loggingEvents = captorLoggingEvent.getAllValues();
        assertTrue(loggingEvents.stream().anyMatch(loggingEvent -> loggingEvent.getFormattedMessage().contains("AuthMethod: Password")));
    }

    @Test
    public void testStartupPublicKey() {
        // given
        ReflectionTestUtils.setField(sftpService, "enabled", true);
        ReflectionTestUtils.setField(sftpService, "port", new Random().nextInt(64512) + 1024);
        ReflectionTestUtils.setField(sftpService, "publicKeyAuthenticationEnabled", true);

        // when
        sftpService.startup();

        // then
        assertThat(sftpService.isRunning(), is(true));
        verify(mockAppender, atLeastOnce()).doAppend(captorLoggingEvent.capture());
        List<LoggingEvent> loggingEvents = captorLoggingEvent.getAllValues();
        assertTrue(loggingEvents.stream().anyMatch(loggingEvent -> loggingEvent.getFormattedMessage().contains("AuthMethod: PublicKey")));
    }

    @Test
    public void testShutdown() {
        // given
        ReflectionTestUtils.setField(sftpService, "enabled", true);
        ReflectionTestUtils.setField(sftpService, "port", new Random().nextInt(64512) + 1024);
        sftpService.startup();
        assertThat(sftpService.isRunning(), is(true));

        // when
        sftpService.shutdown();

        // then
        assertThat(sftpService.isRunning(), is(false));
        verify(mockAppender, atLeastOnce()).doAppend(captorLoggingEvent.capture());
        List<LoggingEvent> loggingEvents = captorLoggingEvent.getAllValues();
        assertTrue(loggingEvents.stream().anyMatch(loggingEvent -> loggingEvent.getFormattedMessage().contains("is down")));
    }

    @Test
    public void testShutdownDisabled() {
        // given
        ReflectionTestUtils.setField(sftpService, "enabled", false);
        sftpService.startup();
        assertThat(sftpService.isRunning(), is(false));

        // when
        sftpService.shutdown();

        // then
        assertThat(sftpService.isRunning(), is(false));
        verify(mockAppender, atLeastOnce()).doAppend(captorLoggingEvent.capture());
        List<LoggingEvent> loggingEvents = captorLoggingEvent.getAllValues();
        assertTrue(loggingEvents.stream().noneMatch(loggingEvent -> loggingEvent.getFormattedMessage().contains("is down")));
    }
}
