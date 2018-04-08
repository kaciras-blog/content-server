package net.kaciras.misc;

import io.reactivex.Single;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.SingleSubject;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.NotSerializableException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

@SuppressWarnings("ResultOfMethodCallIgnored")
final class RxJavaTest {

	/**
	 * CompletableFuture用completeExceptionally()方法设置为出现异常时，会将异常包装
	 * 为ExecutionException，调用方使用get()获取结果时将抛出。
	 *
	 * 该测试验证RxJava中，Single.fromFuture不会解包ExecutionException异常。
	 *
	 * @throws Exception 测试方法抛出了异常
	 */
	@Test
	void verifyUnwarpExceptionFromFuture() throws Exception {
		CompletableFuture<Void> future = new CompletableFuture<>();
		AtomicReference<Object> holder = new AtomicReference<>();

		Single.fromFuture(future)
				.subscribeOn(Schedulers.newThread())
				.subscribe((r, e) -> holder.set(e));

		future.completeExceptionally(new NotSerializableException());
		Thread.sleep(1000);

		Throwable exception = (Throwable) holder.get();
		Assertions.assertThat(exception).isInstanceOf(ExecutionException.class);
		Assertions.assertThat(exception.getCause()).isInstanceOf(NotSerializableException.class);
	}

	@Test
	void as2() throws Exception {
		SingleSubject<Integer> a = SingleSubject.create();
		Single<Integer> out = a;
		a.onSuccess(1);

		AtomicInteger ai = new AtomicInteger();

		Thread.sleep(1666);
		out.subscribeOn(Schedulers.newThread()).subscribe(ai::addAndGet);
		out.subscribeOn(Schedulers.newThread()).subscribe(ai::addAndGet);
		Thread.sleep(666);

		Assertions.assertThat(ai.get()).isEqualTo(2);
	}
}
