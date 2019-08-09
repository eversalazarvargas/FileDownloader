## Android Library to create asynchronous tasks to download files.

**Important**: This library is still work in progress, so it's not functional yet.

First create a configuration of the FileDownloader:

```java
FileDownloaderConfig fileDownloaderConfig = new FileDownloaderConfig.Builder()
                .context(context)
                .filesDirectory(filesDirectory)
                .timeout(30)
                .keepLastDownloadRecords(200)
                .build();
```
Then instantiate the FileDownloader with the configuration:
```java
FileDownloader fileDownloader = FileDownloader(config)
```

Then just start downloading files, and listen to change of statuses of your files:

```java

// Setup the listener for status changes
DownloadListener listener = new AbstractDownloadListener() {

            @Override
            public void onError(StatusEvent status) {
                Error error = status.getError();
            }
            
            @Override
            public void onCancelled(StatusEvent status) {
                // retry
                status.getDownloader().uri(status.getUri())
                        .fileName(status.getFileName())
                        .listener(this)
                        .download();
            }
            
            @Override
            public void onCompleted(StatusEvent status) {
                assertEquals(Status.COMPLETED, status.getStatus());
                assertEquals(1.0, status.getProgress(), 0.05);
            }
};

// Download
DownloadToken token = fileDownloader.uri(Uri.parse("https://www.google.com"))
                .fileName(fileName)
                .listener(listener)
                .timeout(2)
                .directory(directory)
                .download();
```
Cancel your download if desired:
```java
fileDownloader.cancel(token);
```
