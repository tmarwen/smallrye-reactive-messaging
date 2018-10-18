package io.smallrye.reactive.messaging.ack;

import io.reactivex.Flowable;
import io.smallrye.reactive.messaging.annotations.Acknowledgment;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.eclipse.microprofile.reactive.messaging.Outgoing;
import org.eclipse.microprofile.reactive.streams.ProcessorBuilder;
import org.eclipse.microprofile.reactive.streams.PublisherBuilder;
import org.eclipse.microprofile.reactive.streams.ReactiveStreams;
import org.reactivestreams.Processor;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

import javax.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@ApplicationScoped
public class BeanWithMessageProcessors extends SpiedBeanHelper {

  static final String DEFAULT_ACKNOWLEDGMENT = "default-acknowledgment";
  static final String DEFAULT_ACKNOWLEDGMENT_BUILDER = "default-acknowledgment-builder";

  static final String PRE_ACKNOWLEDGMENT = "pre-acknowledgment";
  static final String PRE_ACKNOWLEDGMENT_BUILDER = "pre-acknowledgment-builder";

  static final String MANUAL_ACKNOWLEDGMENT = "manual-acknowledgment";
  static final String MANUAL_ACKNOWLEDGMENT_BUILDER = "manual-acknowledgment-builder";

  static final String NO_ACKNOWLEDGMENT = "no-acknowledgment";
  static final String NO_ACKNOWLEDGMENT_BUILDER = "no-acknowledgment-builder";

  private Map<String, List<String>> sink = new ConcurrentHashMap<>();
  private Map<String, List<String>> acknowledged = new ConcurrentHashMap<>();

  @Incoming("sink-" + DEFAULT_ACKNOWLEDGMENT)
  @Acknowledgment(Acknowledgment.Mode.NONE)
  public CompletionStage<Void> sinkDef(Message<String> ignored) {
    return CompletableFuture.completedFuture(null);
  }

  @Incoming("sink-" + DEFAULT_ACKNOWLEDGMENT_BUILDER)
  @Acknowledgment(Acknowledgment.Mode.NONE)
  public CompletionStage<Void> sinkDefBuilder(Message<String> ignored) {
    return CompletableFuture.completedFuture(null);
  }

  @Incoming("sink-" + PRE_ACKNOWLEDGMENT)
  @Acknowledgment(Acknowledgment.Mode.NONE)
  public CompletionStage<Void> sinkPre(Message<String> ignored) {
    return CompletableFuture.completedFuture(null);
  }

  @Incoming("sink-" + PRE_ACKNOWLEDGMENT_BUILDER)
  @Acknowledgment(Acknowledgment.Mode.NONE)
  public CompletionStage<Void> sinkPreBuilder(Message<String> ignored) {
    return CompletableFuture.completedFuture(null);
  }

  @Incoming("sink-" + NO_ACKNOWLEDGMENT)
  @Acknowledgment(Acknowledgment.Mode.NONE)
  public CompletionStage<Void> sinkNo(Message<String> ignored) {
    return CompletableFuture.completedFuture(null);
  }

  @Incoming("sink-" + NO_ACKNOWLEDGMENT_BUILDER)
  @Acknowledgment(Acknowledgment.Mode.NONE)
  public CompletionStage<Void> sinkNoForBuilder(Message<String> ignored) {
    return CompletableFuture.completedFuture(null);
  }

  @Incoming("sink-" + MANUAL_ACKNOWLEDGMENT)
  @Acknowledgment(Acknowledgment.Mode.NONE)
  public CompletionStage<Void> sinkManual(Message<String> ignored) {
    return CompletableFuture.completedFuture(null);
  }

  @Incoming("sink-" + MANUAL_ACKNOWLEDGMENT_BUILDER)
  @Acknowledgment(Acknowledgment.Mode.NONE)
  public CompletionStage<Void> sinkManualForBuilder(Message<String> ignored) {
    return CompletableFuture.completedFuture(null);
  }

  @Incoming(MANUAL_ACKNOWLEDGMENT)
  @Acknowledgment(Acknowledgment.Mode.MANUAL)
  @Outgoing("sink-" + MANUAL_ACKNOWLEDGMENT)
  public Processor<Message<String>, Message<String>> processorWithAck() {
    return ReactiveStreams.<Message<String>>builder()
      .flatMapCompletionStage(m -> m.ack().thenApply(x -> m))
      .flatMap(m -> ReactiveStreams.of(m, m))
      .peek(m -> processed(MANUAL_ACKNOWLEDGMENT, m))
      .buildRs();
  }

  @Outgoing(MANUAL_ACKNOWLEDGMENT)
  public Publisher<Message<String>> sourceToManualAck() {
    return ReactiveStreams.of("a", "b", "c", "d", "e")
      .map(payload ->
        Message.of(payload, () -> CompletableFuture.runAsync(() -> {
          nap();
          acknowledged(MANUAL_ACKNOWLEDGMENT, payload);
        }))
      ).buildRs();
  }

  @Incoming(NO_ACKNOWLEDGMENT)
  @Acknowledgment(Acknowledgment.Mode.NONE)
  @Outgoing("sink-" + NO_ACKNOWLEDGMENT)
  public Processor<Message<String>, Message<String>> processorWithNoAck() {
    return ReactiveStreams.<Message<String>>builder()
      .flatMap(m -> ReactiveStreams.of(m, m))
      .peek(m -> processed(NO_ACKNOWLEDGMENT, m))
      .buildRs();
  }

  @Outgoing(NO_ACKNOWLEDGMENT)
  public Publisher<Message<String>> sourceToNoAck() {
    return Flowable.fromArray("a", "b", "c", "d", "e")
      .map(payload -> Message.of(payload, () -> {
        nap();
        acknowledged(NO_ACKNOWLEDGMENT, payload);
        return CompletableFuture.completedFuture(null);
      }));
  }

  @Incoming(DEFAULT_ACKNOWLEDGMENT)
  @Outgoing("sink-" + DEFAULT_ACKNOWLEDGMENT)
  public Processor<Message<String>, Message<String>> processorWithAutoAck() {
    return ReactiveStreams.<Message<String>>builder()
      .flatMap(m -> ReactiveStreams.of(m, m))
      .peek(m -> processed(DEFAULT_ACKNOWLEDGMENT, m))
      .buildRs();
  }

  @Outgoing(DEFAULT_ACKNOWLEDGMENT)
  public Publisher<Message<String>> sourceToAutoAck() {
    return Flowable.fromArray("a", "b", "c", "d", "e")
      .map(payload -> Message.of(payload, () -> {
        nap();
        acknowledged(DEFAULT_ACKNOWLEDGMENT, payload);
        return CompletableFuture.completedFuture(null);
      }));
  }

  @Incoming(PRE_ACKNOWLEDGMENT)
  @Acknowledgment(Acknowledgment.Mode.PRE_PROCESSING)
  @Outgoing("sink-" + PRE_ACKNOWLEDGMENT)
  public Processor<Message<String>, Message<String>> processorWithPreAck() {
    return ReactiveStreams.<Message<String>>builder()
      .flatMap(m -> ReactiveStreams.of(m, m))
      .peek(m -> processed(PRE_ACKNOWLEDGMENT, m))
      .buildRs();
  }

  @Outgoing(PRE_ACKNOWLEDGMENT)
  public Publisher<Message<String>> sourceToPreAck() {
    return Flowable.fromArray("a", "b", "c", "d", "e")
      .map(payload -> Message.of(payload, () -> {
        nap();
        acknowledged(PRE_ACKNOWLEDGMENT, payload);
        return CompletableFuture.completedFuture(null);
      }));
  }


  @Incoming(MANUAL_ACKNOWLEDGMENT_BUILDER)
  @Acknowledgment(Acknowledgment.Mode.MANUAL)
  @Outgoing("sink-" + MANUAL_ACKNOWLEDGMENT_BUILDER)
  public ProcessorBuilder<Message<String>, Message<String>> processorWithAckWithBuilder() {
    return ReactiveStreams.<Message<String>>builder()
      .flatMapCompletionStage(m -> m.ack().thenApply(x -> m))
      .flatMap(m -> ReactiveStreams.of(m, m))
      .peek(m -> processed(MANUAL_ACKNOWLEDGMENT_BUILDER, m));
  }

  @Outgoing(MANUAL_ACKNOWLEDGMENT_BUILDER)
  public PublisherBuilder<Message<String>> sourceToManualAckWithBuilder() {
    return ReactiveStreams.of("a", "b", "c", "d", "e")
      .map(payload ->
        Message.of(payload, () -> CompletableFuture.runAsync(() -> {
          nap();
          acknowledged(MANUAL_ACKNOWLEDGMENT_BUILDER, payload);
        }))
      );
  }

  @Incoming(NO_ACKNOWLEDGMENT_BUILDER)
  @Acknowledgment(Acknowledgment.Mode.NONE)
  @Outgoing("sink-" + NO_ACKNOWLEDGMENT_BUILDER)
  public ProcessorBuilder<Message<String>, Message<String>> processorWithNoAckWithBuilder() {
    return ReactiveStreams.<Message<String>>builder()
      .flatMap(m -> ReactiveStreams.of(m, m))
      .peek(m -> processed(NO_ACKNOWLEDGMENT_BUILDER, m));
  }

  @Outgoing(NO_ACKNOWLEDGMENT_BUILDER)
  public Publisher<Message<String>> sourceToNoAckWithBuilder() {
    return Flowable.fromArray("a", "b", "c", "d", "e")
      .map(payload -> Message.of(payload, () -> {
        nap();
        acknowledged(NO_ACKNOWLEDGMENT_BUILDER, payload);
        return CompletableFuture.completedFuture(null);
      }));
  }

  @Incoming(DEFAULT_ACKNOWLEDGMENT_BUILDER)
  @Outgoing("sink-" + DEFAULT_ACKNOWLEDGMENT_BUILDER)
  public ProcessorBuilder<Message<String>, Message<String>> processorWithAutoAckBuilder() {
    return ReactiveStreams.<Message<String>>builder()
      .flatMap(m -> ReactiveStreams.of(m, m))
      .peek(m -> processed(DEFAULT_ACKNOWLEDGMENT_BUILDER, m));
  }

  @Outgoing(DEFAULT_ACKNOWLEDGMENT_BUILDER)
  public Publisher<Message<String>> sourceToAutoAckWithBuilder() {
    return Flowable.fromArray("a", "b", "c", "d", "e")
      .map(payload -> Message.of(payload, () -> {
        nap();
        acknowledged(DEFAULT_ACKNOWLEDGMENT_BUILDER, payload);
        return CompletableFuture.completedFuture(null);
      }));
  }

  @Incoming(PRE_ACKNOWLEDGMENT_BUILDER)
  @Acknowledgment(Acknowledgment.Mode.PRE_PROCESSING)
  @Outgoing("sink-" + PRE_ACKNOWLEDGMENT_BUILDER)
  public ProcessorBuilder<Message<String>, Message<String>> processorWithPreAckBuilder() {
    return ReactiveStreams.<Message<String>>builder()
      .flatMap(m -> ReactiveStreams.of(m, m))
      .peek(m -> processed(PRE_ACKNOWLEDGMENT_BUILDER, m));
  }

  @Outgoing(PRE_ACKNOWLEDGMENT_BUILDER)
  public Publisher<Message<String>> sourceToPreAckWithBuilder() {
    return Flowable.fromArray("a", "b", "c", "d", "e")
      .map(payload -> Message.of(payload, () -> {
        nap();
        acknowledged(PRE_ACKNOWLEDGMENT_BUILDER, payload);
        return CompletableFuture.completedFuture(null);
      }));
  }

}
