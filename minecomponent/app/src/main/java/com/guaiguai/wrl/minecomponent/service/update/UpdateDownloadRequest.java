package com.guaiguai.wrl.minecomponent.service.update;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;

/**********************************************************
 * @文件名称：UpdateDownloadRequest.java
 * @文件作者：renzhiqiang
 * @创建时间：2015年8月26日 下午10:58:03
 * @文件描述：真正执行下载任务的Runnable
 * @修改历史：2015年8月26日创建初始版本
 **********************************************************/
public class UpdateDownloadRequest implements Runnable {
	private int startPos = 0;
	private String downloadUrl;
	private String localFilePath;
	private UpdateDownloadListener downloadListener;
	private DownloadResponseHandler downloadHandler;
	private boolean isDownloading = false;
	private long contentLength;

	public UpdateDownloadRequest(String downloadUrl, String localFilePath, UpdateDownloadListener downloadListener) {
		this.downloadUrl = downloadUrl;
		this.localFilePath = localFilePath;
		this.downloadListener = downloadListener;
		downloadHandler = new DownloadResponseHandler();
		isDownloading = true;
	}

	/**
	 * 真正的去建立连接
	 * @throws IOException
	 * @throws InterruptedException
	 */
	private void makeRequest() throws IOException, InterruptedException {
		//判断当前线程是否被打断
		if (!Thread.currentThread().isInterrupted()) {
			try {
				URL url = new URL(downloadUrl);
				HttpURLConnection connection = (HttpURLConnection) url.openConnection();
				connection.setConnectTimeout(5000);
				connection.setRequestMethod("GET");
				connection.setRequestProperty("Range", "bytes=" + startPos + "-");
				connection.setRequestProperty("Connection", "Keep-Alive");
				connection.connect();//阻塞当前的线程所以要放入到子线程中
				contentLength = connection.getContentLength();
				if (!Thread.currentThread().isInterrupted()) {
					if (downloadHandler != null) {
						downloadHandler.sendResponseMessage(connection.getInputStream());//取得与远程文件的流
					}
				}
			} catch (IOException e) {
				if (!Thread.currentThread().isInterrupted()) {
					throw e;
				}
			}
		}
	}

	@Override
	public void run() {
		try {
			makeRequest();
		} catch (IOException e) {
			if (downloadHandler != null) {
				downloadHandler.sendFailureMessage(FailureCode.IO);
			}
		} catch (InterruptedException e) {
			if (downloadHandler != null) {
				downloadHandler.sendFailureMessage(FailureCode.Interrupted);
			}
		}
	}

	public boolean isDownloading() {
		return isDownloading;
	}

	public void stopDownloading() {
		isDownloading = false;
	}

	/**********************************************************
	 * @文件名称：UpdateDownloadRequest.java
	 * @文件作者：renzhiqiang
	 * @创建时间：2015年8月3日 上午11:16:40
	 * @文件描述：下载消息转发器
	 * @修改历史：2015年8月3日创建初始版本
	 **********************************************************/
	public class DownloadResponseHandler {
		protected static final int SUCCESS_MESSAGE = 0;
		protected static final int FAILURE_MESSAGE = 1;
		protected static final int START_MESSAGE = 2;
		protected static final int FINISH_MESSAGE = 3;
		protected static final int NETWORK_OFF = 4;
		private Handler handler;

		public DownloadResponseHandler() {
			if (Looper.myLooper() != null) {
				handler = new Handler() {
					@Override
					public void handleMessage(Message msg) {
						handleSelfMessage(msg);
					}
				};

			}
		}

		public void onFinish() {
			downloadListener.onFinished(mCompleteSize, "");
		}

		public void onFailure(FailureCode failureCode) {
			downloadListener.onFailure();
		}

		/**
		 * 用来发送不同的消息
		 */
		private void sendPausedMessage() {
			sendMessage(obtainMessage(PAUSED_MESSAGE, null));
		}

		private void sendProgressChangedMessage(int progress) {
			sendMessage(obtainMessage(PROGRESS_CHANGED, new Object[] { progress }));
		}

		protected void sendFailureMessage(FailureCode failureCode) {
			sendMessage(obtainMessage(FAILURE_MESSAGE, new Object[] { failureCode }));
		}

		//
		// Pre-processing of messages (in original calling thread, typically the
		// UI thread)
		//

		protected void handlePausedMessage() {
			downloadListener.onPaused(progress, mCompleteSize, "");
		}

		protected void handleProgressChangedMessage(int progress) {
			downloadListener.onProgressChanged(progress, "");
		}

		protected void handleFailureMessage(FailureCode failureCode) {
			onFailure(failureCode);
		}

		protected void sendFinishMessage() {
			sendMessage(obtainMessage(FINISH_MESSAGE, null));
		}

		// Methods which emulate android's Handler and Message methods
		protected void handleSelfMessage(Message msg) {
			Object[] response;
			switch (msg.what) {
			case FAILURE_MESSAGE:
				response = (Object[]) msg.obj;
				handleFailureMessage((FailureCode) response[0]);
				break;
			case PROGRESS_CHANGED:
				response = (Object[]) msg.obj;
				handleProgressChangedMessage(((Integer) response[0]).intValue());
				break;
			case PAUSED_MESSAGE:
				handlePausedMessage();
				break;
			case FINISH_MESSAGE:
				onFinish();
				break;
			}
		}

		protected void sendMessage(Message msg) {
			if (handler != null) {
				handler.sendMessage(msg);
			} else {
				handleSelfMessage(msg);
			}
		}


		/**
		 * 获取一个消息对象
		 * @param responseMessage
		 * @param response
		 * @return
		 */
		protected Message obtainMessage(int responseMessage, Object response) {
			Message msg = null;
			if (handler != null) {
				msg = this.handler.obtainMessage(responseMessage, response);
			} else {
				msg = Message.obtain();
				msg.what = responseMessage;
				msg.obj = response;
			}
			return msg;
		}

		/**
		 * 格式化数据
		 * @param value
		 * @return
		 */
		private String getTwoPointFloatStr(float value) {
			DecimalFormat fnum = new DecimalFormat("0.00");
			return fnum.format(value);
		}

		private int mCompleteSize = 0;
		private int progress = 0;
		private static final int PROGRESS_CHANGED = 5;
		private static final int PAUSED_MESSAGE = 7;

		/**
		 * 开始真正的下载
		 * @param is
		 */
		void sendResponseMessage(InputStream is) {
			RandomAccessFile randomAccessFile = null;
			mCompleteSize = 0;
			try {
				byte[] buffer = new byte[1024];
				int length = -1;
				int limit = 0;
				randomAccessFile = new RandomAccessFile(localFilePath, "rwd");
				randomAccessFile.seek(startPos);
				boolean isPaused = false;
				while ((length = is.read(buffer)) != -1) {
					if (isDownloading) {
						randomAccessFile.write(buffer, 0, length);
						mCompleteSize += length;
						if (mCompleteSize < contentLength) {
							//计算进度
							progress = (int) (Float.parseFloat(getTwoPointFloatStr(
									(float) (startPos + mCompleteSize) / (contentLength + startPos))) * 100);
							if (limit % 30 == 0 || progress == 100) {
								//为了限制通知更新的频率  没30次更新一次  或者是下载完全的时候进行更新
								sendProgressChangedMessage(progress); //在子线程中读取流数据，后转发到主线程中去。
							}
						}
						limit++;
					} else {
						isPaused = true;
						sendPausedMessage();
						break;
					}
				}
				stopDownloading();
				if (!isPaused) {
					sendFinishMessage();
				}
			} catch (IOException e) {
				sendPausedMessage();
				stopDownloading();
				e.printStackTrace();
			} finally {
				try {
					if (is != null) {
						is.close();
					}
					if (randomAccessFile != null) {
						randomAccessFile.close();
					}
				} catch (IOException e) {
					stopDownloading();
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * 包括下载的所有的异常
	 */
	public enum FailureCode {
		UnknownHost, Socket, SocketTimeout, ConnectTimeout, IO, HttpResponse, JSON, Interrupted
	}
}