package net.kaciras.misc;

import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.SingleSubject;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

final class RxJavaTest {

	@Test
	void as() throws InterruptedException {
		CompletableFuture f = new CompletableFuture();
		AtomicReference ar = new AtomicReference();

		//fromFuture不会解包ExecutionException异常
		Single.fromFuture(f)
				.subscribeOn(Schedulers.newThread())
				.doOnError(ar::set).subscribe();

		Thread.sleep(666);
		f.completeExceptionally(new IOException());
		Thread.sleep(666);

		Assertions.assertThat(ar.get()).isInstanceOf(IOException.class);
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
