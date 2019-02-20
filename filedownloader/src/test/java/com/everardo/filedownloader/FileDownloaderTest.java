package com.everardo.filedownloader;

import android.content.Context;
import android.net.Uri;

import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FileDownloaderTest {

    private final String userDir = System.getProperty("user.dir");
    private FileDownloader fileDownloader;
    private FileDownloaderConfig fileDownloaderConfig;
    private DownloadRegistry downloadRegistry = mock(DownloadRegistry.class);
    private Uri uri = mock(Uri.class);
    private Context context = mock(Context.class);

    @Before
    public void setup() {
        File filesDirectory = new File(userDir);

        fileDownloaderConfig = new FileDownloaderConfig.Builder()
                .context(context)
                .filesDirectory(filesDirectory)
                .timeout(30)
                .keepLastDownloadRecords(200)
                .build();

        fileDownloader = mock(FileDownloader.class);

        FileDownloader.RequestCreator requestCreator = new FileDownloader.RequestCreator(fileDownloader, uri);
        when(fileDownloader.uri(any(Uri.class))).thenReturn(requestCreator);

    }

    @Test
    public void constructor() {
        File filesDirectory = new File(userDir);

        assertEquals(filesDirectory, fileDownloaderConfig.getDirectory());
    }

    @Test
    public void notExistingDirectory() {
        File filesDirectory = new File("non/existing");

        try {
            new FileDownloaderConfig.Builder()
                    .context(context)
                    .filesDirectory(filesDirectory)
                    .build();
        } catch (IllegalStateException ex) {
            assertEquals("Directory must exists", ex.getMessage());
        }
    }

    @Test
    public void notDirectory() throws IOException {
        File parent = new File(userDir + "/src/test/resources");
        File someFile = new File(parent, "somefile");
        someFile.createNewFile();

        assertTrue(someFile.exists());

        try {
            new FileDownloaderConfig.Builder()
                    .context(context)
                    .filesDirectory(someFile)
                    .build();
        } catch (IllegalStateException ex) {
            assertEquals("Directory parameter is not a directory", ex.getMessage());
        }
    }

    @Test
    public void download() {
        final String fileName = "myfile";

        doAnswer(new Answer<DownloadToken>() {

            @Override
            public DownloadToken answer(InvocationOnMock invocation) throws Throwable {
                DownloadListener listener = invocation.getArgument(2);
                StatusEvent response = new StatusEvent(Status.COMPLETED, 1.0, fileDownloader, uri, fileName, null);
                listener.onCompleted(response);

                return new DownloadToken();
            }
        }).when(fileDownloader).download(any(Uri.class), anyString(), any(DownloadListener.class), anyLong(), any(File.class));


        DownloadListener testListener = new DownloadListener() {
            @Override
            public void onCompleted(@NotNull StatusEvent status) {
                status.component2();
                assertEquals(Status.COMPLETED, status.getStatus());
                assertEquals(1.0, status.getProgress(), 0.05);
            }

            @Override
            public void onProgress(@NotNull StatusEvent status) {

            }

            @Override
            public void onCancelled(@NotNull StatusEvent status) {

            }

            @Override
            public void onError(@NotNull StatusEvent status) {

            }
        };

        DownloadToken token = fileDownloader.uri(uri)
                .fileName(fileName)
                .listener(testListener)
                .timeout(2)
                .directory(new File("hello"))
                .download();

        assertNotNull(token);
    }

    @Test
    public void error() {
        final String fileName = "myfile";

        doAnswer(new Answer<DownloadToken>() {

            @Override
            public DownloadToken answer(InvocationOnMock invocation) throws Throwable {
                DownloadListener listener = invocation.getArgument(2);
                Error error = new Error(Error.Code.HTTP_ERROR, 400);
                StatusEvent response = new StatusEvent(Status.ERROR, 1.0, fileDownloader, uri, fileName, error);
                listener.onError(response);

                return new DownloadToken();
            }
        }).when(fileDownloader).download(any(Uri.class), anyString(), any(DownloadListener.class), anyLong(), any(File.class));


        DownloadListener testListener = new DownloadListener() {
            @Override
            public void onCompleted(@NotNull StatusEvent status) {
            }

            @Override
            public void onProgress(@NotNull StatusEvent status) {

            }

            @Override
            public void onCancelled(@NotNull StatusEvent status) {

            }

            @Override
            public void onError(@NotNull StatusEvent status) {
                assertEquals(Status.ERROR, status.getStatus());

                Error error = status.getError();
                if (error != null) {
                    switch (error.getCode()) {
                        case DOWNLOAD_ALREADY_IN_PROGRESS:
                            break;
                        case HTTP_ERROR:
                            assertNotNull(error.getHttpErrorCode());
                            break;
                        default:
                            break;
                    }
                }
            }
        };

        DownloadToken token = fileDownloader.uri(uri)
                .fileName(fileName)
                .listener(testListener)
                .timeout(2)
                .directory(new File("hello"))
                .download();

        assertNotNull(token);
    }

    @Test
    public void cancel() {
        final String fileName = "myfile";

        final DownloadListener testListener = new DownloadListener() {
            @Override
            public void onCompleted(@NotNull StatusEvent status) {

            }

            @Override
            public void onProgress(@NotNull StatusEvent status) {
                assertEquals(Status.IN_PROGRESS, status.getStatus());
                assertNotEquals(0, status.getProgress(), 0.05);
            }

            @Override
            public void onCancelled(@NotNull StatusEvent status) {
                assertEquals(Status.CANCELLED, status.getStatus());

                // retry
                status.getDownloader().uri(status.getUri())
                        .fileName(status.getFileName())
                        .listener(this)
                        .download();
            }

            @Override
            public void onError(@NotNull StatusEvent status) {

            }
        };

        doAnswer(new Answer<DownloadToken>() {

            @Override
            public DownloadToken answer(InvocationOnMock invocation) throws Throwable {
                DownloadListener listener = invocation.getArgument(2);
                StatusEvent response = new StatusEvent(Status.IN_PROGRESS, 0.5, fileDownloader, uri, fileName, null);
                listener.onProgress(response);

                return new DownloadToken();
            }
        }).when(fileDownloader).download(any(Uri.class), anyString(), any(DownloadListener.class), anyLong(), any(File.class));

        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) {
                StatusEvent response = new StatusEvent(Status.CANCELLED, 0.5, fileDownloader, uri, fileName, null);
                testListener.onCancelled(response);

                return null;
            }
        }).when(fileDownloader).cancel(any(DownloadToken.class));


        DownloadToken token = fileDownloader.uri(uri)
                .fileName(fileName)
                .listener(testListener)
                .timeout(2)
                .directory(new File("hello"))
                .download();

        fileDownloader.cancel(token);
    }

    @Test
    public void registry() {
        final String fileName = "myfile";

        final DownloadToken token = mock(DownloadToken.class);

        DownloadInfo item = mock(DownloadInfo.class);
        when(item.getStatus()).thenReturn(Status.COMPLETED);
        when(item.getDownloadToken()).thenReturn(token);
        List<DownloadInfo> completed = new ArrayList<>();
        completed.add(item);
        when(downloadRegistry.getCompleted()).thenReturn(completed);

        when(fileDownloader.getDownloadRegistry()).thenReturn(downloadRegistry);

        doAnswer(new Answer<DownloadToken>() {

            @Override
            public DownloadToken answer(InvocationOnMock invocation) throws Throwable {
                DownloadListener listener = invocation.getArgument(2);
                StatusEvent response = new StatusEvent(Status.COMPLETED, 1.0, fileDownloader, uri, fileName, null);
                listener.onCompleted(response);

                return token;
            }
        }).when(fileDownloader).download(any(Uri.class), anyString(), any(DownloadListener.class), anyLong(), any(File.class));


        DownloadListener testListener = new DownloadListener() {
            @Override
            public void onCompleted(@NotNull StatusEvent status) {
                assertEquals(Status.COMPLETED, status.getStatus());
                assertEquals(1.0, status.getProgress(), 0.05);
            }

            @Override
            public void onProgress(@NotNull StatusEvent status) {

            }

            @Override
            public void onCancelled(@NotNull StatusEvent status) {

            }

            @Override
            public void onError(@NotNull StatusEvent status) {

            }
        };

        DownloadToken tokenResult = fileDownloader.uri(uri)
                .fileName(fileName)
                .listener(testListener)
                .timeout(2)
                .directory(new File("hello"))
                .download();

        assertNotNull(tokenResult);

        // the following is supposed to run in a background thread
        List<DownloadInfo> completedResult = fileDownloader.getDownloadRegistry().getCompleted(new Date(), new Date());
        assertEquals(tokenResult, completedResult.get(0).getDownloadToken());
    }

}
