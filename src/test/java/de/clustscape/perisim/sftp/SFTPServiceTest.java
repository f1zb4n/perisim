package de.clustscape.perisim.sftp;

import de.clustscape.perisim.service.sftp.SFTPService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Random;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Tests the SFTPService.
 *
 * @author Marco Werner
 */
@ExtendWith(MockitoExtension.class)
public class SFTPServiceTest {

    @InjectMocks
    private SFTPService sftpService;

    @Test
    public void testStartupDisabled() {
        // given

        // when
        sftpService.startup();

        // then
        assertThat(sftpService.isRunning(), is(false));
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
        sftpService.shutdown();
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
        sftpService.shutdown();
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
    }
}
