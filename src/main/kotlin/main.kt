
import io.reactivex.rxjava3.core.*
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.*
import io.reactivex.rxjava3.processors.PublishProcessor
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subjects.AsyncSubject
import io.reactivex.rxjava3.subjects.BehaviorSubject
import io.reactivex.rxjava3.subjects.PublishSubject
import io.reactivex.rxjava3.subjects.ReplaySubject
import java.io.File
import java.io.FileNotFoundException
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt
import kotlin.text.Charsets.UTF_8

fun main(args: Array<String>) {
    chapter2()
    chapter3()
    chapter5()
    chapter7()
    chapter9()
//  chapter14()
}

// Operators
fun chapter2() {

    exampleOf("just") {
        val observable: Observable<Int> = Observable.just(1)
    }

    exampleOf("fromIterable") {
        val observable: Observable<Int> = Observable.fromIterable(listOf(1, 2, 3))
    }

    exampleOf("subscribe") {

        val observable = Observable.just(1, 2, 3)

        observable.subscribe { element ->
            println(element)
        }
    }

    exampleOf("empty") {

        val observable = Observable.empty<Unit>()

        observable.subscribeBy(
          onNext = { println(it) },
          onComplete = { println("Completed") }
        )
    }

    exampleOf("never") {

        val observable = Observable.never<Any>()

        observable.subscribeBy(
          onNext = { println(it) },
          onComplete = { println("Completed") }
        )
    }

    exampleOf("range") {
        val observable = Observable.range(1, 10)

        observable.subscribe {
            val n = it.toDouble()
            val fibonacci = ((Math.pow(1.61803, n) - Math.pow(0.61803, n)) / 2.23606).roundToInt()
            println(fibonacci)
        }
    }

    exampleOf("dispose") {

        val mostPopular: Observable<String> = Observable.just("A", "B", "C")

        val subscription = mostPopular.subscribe {
            println(it)
        }

        subscription.dispose()
    }

    exampleOf("CompositeDisposable") {

        val subscriptions = CompositeDisposable()
        val disposable = Observable.just("A", "B", "C")
          .subscribe {
              println(it)
          }

        subscriptions.add(disposable)

        subscriptions.dispose()
    }

    exampleOf("create") {
        Observable.create<String> { emitter ->
            emitter.onNext("1")

//    emitter.onError(RuntimeException("Error"))
//    emitter.onComplete()

            emitter.onNext("?")
        }.subscribeBy(
          onNext = { println(it) },
          onComplete = { println("Completed") },
          onError = { println("Error") }
        )
    }

    exampleOf("defer") {

        val disposables = CompositeDisposable()

        var flip = false

        val factory: Observable<Int> = Observable.defer {
            flip = !flip

            if (flip) {
                Observable.just(1, 2, 3)
            } else {
                Observable.just(4, 5, 6)
            }
        }

        for (i in 0..3) {
            disposables.add(
              factory.subscribe {
                  println(it)
              }
            )
        }

        disposables.dispose()
    }

    exampleOf("Single") {
        val subscriptions = CompositeDisposable()

        fun loadText(filename: String): Single<String> {
            return Single.create create@{ emitter ->
                val file = File(filename)

                if (!file.exists()) {
                    emitter.onError(FileNotFoundException("Can't find $filename"))
                    return@create
                }

                val contents = file.readText(UTF_8)

                emitter.onSuccess(contents)
            }
        }

        val observer = loadText("Copyright.txt")
          .subscribeBy(
            onSuccess = { println(it) },
            onError = { println("Error, $it") }
          )

        subscriptions.add(observer)
    }
}

/*subject*/
fun chapter3() {
    exampleOf("PublishSubject") {

        val publishSubject = PublishSubject.create<Int>()

        publishSubject.onNext(1)

        val subscriptionOne = publishSubject.subscribeBy(
          onNext = { printWithLabel("1)", it) },
          onComplete = { printWithLabel("1)", "Complete") }
        )

        publishSubject.onNext(2)

        val subscriptionTwo = publishSubject.subscribeBy(
          onNext = { printWithLabel("2)", it) },
          onComplete = { printWithLabel("2)", "Complete") }
        )

        publishSubject.onNext(3)

        subscriptionOne.dispose()

        publishSubject.onNext(4)

        publishSubject.onComplete()

        publishSubject.onNext(5)

        subscriptionTwo.dispose()

        val subscriptionThree = publishSubject.subscribeBy(
          onNext = { printWithLabel("3)", it) },
          onComplete = { printWithLabel("3)", "Complete") }
        )

        publishSubject.onNext(6)

        subscriptionThree.dispose()
    }


    exampleOf("BehaviorSubject") {

        val subscriptions = CompositeDisposable()

        val behaviorSubject = BehaviorSubject.createDefault("Initial value")

        behaviorSubject.onNext("X")

        val subscriptionOne = behaviorSubject.subscribeBy(
          onNext = { printWithLabel("1)", it) },
          onError = { printWithLabel("1)", it) }
        )

        subscriptions.add(subscriptionOne)

        behaviorSubject.onError(RuntimeException("Error!"))

        subscriptions.add(behaviorSubject.subscribeBy(
          onNext = { printWithLabel("2)", it) },
          onError = { printWithLabel("2)", it) }
        ))

        subscriptions.dispose()
    }

    exampleOf("BehaviorSubject State") {

        val subscriptions = CompositeDisposable()

        val behaviorSubject = BehaviorSubject.createDefault(0)

        println(behaviorSubject.value)

        subscriptions.add(behaviorSubject.subscribeBy {
            printWithLabel("1)", it)
        })

        behaviorSubject.onNext(1)
        println(behaviorSubject.value)

        subscriptions.dispose()
    }

    exampleOf("ReplaySubject") {

        val subscriptions = CompositeDisposable()

        val replaySubject = ReplaySubject.createWithSize<String>(2)

        replaySubject.onNext("1")

        replaySubject.onNext("2")

        replaySubject.onNext("3")

        subscriptions.add(replaySubject.subscribeBy(
          onNext = { printWithLabel("1)", it) },
          onError = { printWithLabel("1)", it)}
        ))

        subscriptions.add(replaySubject.subscribeBy(
          onNext = { printWithLabel("2)", it) },
          onError = { printWithLabel("2)", it)}
        ))

        replaySubject.onNext("4")

        replaySubject.onError(RuntimeException("Error!"))

        subscriptions.add(replaySubject.subscribeBy(
          onNext = { printWithLabel("3)", it) },
          onError = { printWithLabel("3)", it)}
        ))

        subscriptions.dispose()
    }

    exampleOf("AsyncSubject") {
        val subscriptions = CompositeDisposable()

        val asyncSubject = AsyncSubject.create<Int>()

        subscriptions.add(asyncSubject.subscribeBy(
          onNext = { printWithLabel("1)", it) },
          onComplete = { printWithLabel("1)", "Complete") }
        ))

        asyncSubject.onNext(0)
        asyncSubject.onNext(1)
        asyncSubject.onNext(2)
        asyncSubject.onComplete()

        subscriptions.dispose()
    }

//    exampleOf("RxRelay") {
//      val subscriptions = CompositeDisposable()
//
//      val publishRelay = PublishRelay.create<Int>()
//
//      subscriptions.add(publishRelay.subscribeBy(
//        onNext = { printWithLabel("1)", it) }
//      ))
//
//      publishRelay.accept(1)
//      publishRelay.accept(2)
//      publishRelay.accept(3)
//    }
}

/*Filtering Operators */
fun chapter5() {
    exampleOf("ignoreElements") {
        val subscriptions = CompositeDisposable()

        val strikes = PublishSubject.create<String>()

        subscriptions.add(
          strikes.ignoreElements() // Returns a Completable, so no onNext in subscribeBy
            .subscribeBy {
                println("You're out!")
            })

        strikes.onNext("X")
        strikes.onNext("X")
        strikes.onNext("X")

        strikes.onComplete()
        // Disposing of after use
        subscriptions.dispose()
    }

    exampleOf("elementAt") {

        val subscriptions = CompositeDisposable()

        val strikes = PublishSubject.create<String>()

        subscriptions.add(
          strikes.elementAt(2) // Returns a Maybe, subscribe with onSuccess instead of onNext
            .subscribeBy(
              onSuccess = { println("You're out!") }
            ))

        strikes.onNext("X")
        strikes.onNext("X")
        strikes.onNext("X")

        // Disposing of after use
        subscriptions.dispose()
    }

    exampleOf("filter") {

        val subscriptions = CompositeDisposable()

        subscriptions.add(
          Observable.fromIterable(listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10))
            .filter { number ->
                number > 5
            }.subscribe {
                println(it)
            })

        // Disposing of after use
        subscriptions.dispose()
    }

    exampleOf("skip") {
        val subscriptions = CompositeDisposable()

        subscriptions.add(
          Observable.just("A", "B", "C", "D", "E", "F")
            .skip(3)
            .subscribe {
                println(it)
            })

        // Disposing of after use
        subscriptions.dispose()
    }

    exampleOf("skipWhile") {

        val subscriptions = CompositeDisposable()

        subscriptions.add(
          Observable.just(2, 2, 3, 4)
            .skipWhile { number ->
                number % 2 == 0
            }.subscribe {
                println(it)
            })

        // Disposing of after use
        subscriptions.dispose()
    }

    exampleOf("skipUntil") {

        val subscriptions = CompositeDisposable()

        val subject = PublishSubject.create<String>()
        val trigger = PublishSubject.create<String>()

        subscriptions.add(
          subject.skipUntil(trigger)
            .subscribe {
                println(it)
            })

        subject.onNext("A")
        subject.onNext("B")

        trigger.onNext("X")

        subject.onNext("C")

        // Disposing of after use
        subscriptions.dispose()
    }

    exampleOf("take") {
        val subscriptions = CompositeDisposable()

        subscriptions.add(
          Observable.just(1, 2, 3, 4, 5, 6)
            .take(3)
            .subscribe {
                println(it)
            })

        // Disposing of after use
        subscriptions.dispose()
    }

    exampleOf("takeWhile") {
        val subscriptions = CompositeDisposable()

        subscriptions.add(
          Observable.fromIterable(listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 1))
            .takeWhile { number ->
                number < 5
            }.subscribe {
                println(it)
            })

        // Disposing of after use
        subscriptions.dispose()
    }

    exampleOf("takeUntil") {
        val subscriptions = CompositeDisposable()

        val subject = PublishSubject.create<String>()
        val trigger = PublishSubject.create<String>()

        subscriptions.add(
          subject.takeUntil(trigger)
            .subscribe {
                println(it)
            })

        subject.onNext("1")
        subject.onNext("2")

        trigger.onNext("X")

        subject.onNext("3")

        // Disposing of after use
        subscriptions.dispose()
    }

    exampleOf("distinctUntilChanged") {

        val subscriptions = CompositeDisposable()

        subscriptions.add(
          Observable.just("Dog", "Cat", "Cat", "Dog")
            .distinctUntilChanged()
            .subscribe {
                println(it)
            })

        // Disposing of after use
        subscriptions.dispose()
    }

    exampleOf("distinctUntilChangedPredicate") {

        val subscriptions = CompositeDisposable()

        subscriptions.add(
          Observable.just("ABC", "BCD", "CDE", "FGH", "IJK", "JKL", "LMN")
            .distinctUntilChanged { first, second ->
                second.any { it in first }
            }
            .subscribe {
                println(it)
            }
        )

        // Disposing of after use
        subscriptions.dispose()
    }
}

/*Transforming Operators*/
fun chapter7() {
    exampleOf("map") {

        val subscriptions = CompositeDisposable()

        subscriptions.add(
          Observable.just("M", "C", "V", "I")
            .map {
                it.romanNumeralIntValue()
            }
            .subscribeBy {
                println(it)
            })
    }

    exampleOf("flatMap") {

        val subscriptions = CompositeDisposable()

        val ryan = Student(BehaviorSubject.createDefault(80))
        val charlotte = Student(BehaviorSubject.createDefault(90))

        val student = PublishSubject.create<Student>()

        student
          .flatMap { it.score }
          .subscribe { println(it) }
          .addTo(subscriptions)

        student.onNext(ryan)
        ryan.score.onNext(95)

        student.onNext(charlotte)
        ryan.score.onNext(5)

        charlotte.score.onNext(100)
    }

    exampleOf("switchMap") {

        val ryan = Student(BehaviorSubject.createDefault(80))
        val charlotte = Student(BehaviorSubject.createDefault(90))

        val student = PublishSubject.create<Student>()

        student
          .switchMap { it.score }
          .subscribe { println(it) }

        student.onNext(ryan)

        ryan.score.onNext(85)

        student.onNext(charlotte)

        ryan.score.onNext(95)

        charlotte.score.onNext(100)
    }

    exampleOf("toList") {

        val subscriptions = CompositeDisposable()

        val items = Observable.just("A", "B", "C")

        subscriptions.add(
          items
            .toList()
            .subscribeBy {
                println(it)
            }
        )
    }

    exampleOf("materialize/dematerialize") {

        val subscriptions = CompositeDisposable()

        val ryan = Student(BehaviorSubject.createDefault(80))
        val charlotte = Student(BehaviorSubject.createDefault(90))

        val student = BehaviorSubject.createDefault<Student>(ryan)

        val studentScore = student
          .switchMap { it.score.materialize() }

        studentScore
          .filter {
              if (it.error != null) {
                  println(it.error)
                  false
              } else {
                  true
              }
          }
          .dematerialize { it }
          .subscribe {
              println(it)
          }
          .addTo(subscriptions)

        ryan.score.onNext(85)

        ryan.score.onError(RuntimeException("Error!"))

        ryan.score.onNext(90)

        student.onNext(charlotte)
    }
}

/*combining-operators*/
fun chapter9() {
    exampleOf("startWith") {

        val subscriptions = CompositeDisposable()
        val missingNumbers = Observable.just(3, 4, 5)
        val completeSet = missingNumbers.startWithIterable(listOf(1, 2))

        completeSet
          .subscribe { number ->
              println(number)
          }
          .addTo(subscriptions)
    }

    exampleOf("concat") {

        val subscriptions = CompositeDisposable()
        val first = Observable.just(1, 2, 3, 4)
        val second = Observable.just(4, 5, 6)
        Observable.concat(first, second)
          .subscribe { number ->
              println(number)
          }
          .addTo(subscriptions)
    }

    exampleOf("concatWith") {

        val subscriptions = CompositeDisposable()

        val germanCities = Observable.just("Berlin", "Münich", "Frankfurt")
        val spanishCities = Observable.just("Madrid", "Barcelona", "Valencia")


        germanCities
          .concatWith(spanishCities)
          .subscribe { number ->
              println(number)
          }
          .addTo(subscriptions)
    }

    exampleOf("concatMap") {
        val subscriptions = CompositeDisposable()

        val countries = Observable.just("Germany", "Spain")

        val observable = countries
          .concatMap {
              when (it) {
                  "Germany" -> Observable.just("Berlin", "Münich", "Frankfurt")
                  "Spain" -> Observable.just("Madrid", "Barcelona", "Valencia")
                  else -> Observable.empty<String>()
              }
          }


        observable
          .subscribe { city ->
              println(city)
          }
          .addTo(subscriptions)
    }

    exampleOf("merge") {

        val subscriptions = CompositeDisposable()

        val left = PublishSubject.create<Int>()
        val right = PublishSubject.create<Int>()

        Observable.merge(left, right)
          .subscribe {
              println(it)
          }
          .addTo(subscriptions)

        left.onNext(0)
        left.onNext(1)
        right.onNext(3)
        left.onNext(4)
        right.onNext(5)
        right.onNext(6)
    }

    exampleOf("mergeWith") {

        val subscriptions = CompositeDisposable()

        val germanCities = PublishSubject.create<String>()
        val spanishCities = PublishSubject.create<String>()

        germanCities.mergeWith(spanishCities)
          .subscribe {
              println(it)
          }
          .addTo(subscriptions)

        germanCities.onNext("Frankfurt")
        germanCities.onNext("Berlin")
        spanishCities.onNext("Madrid")
        germanCities.onNext("Münich")
        spanishCities.onNext("Barcelona")
        spanishCities.onNext("Valencia")
    }

    exampleOf("combineLatest") {

        val subscriptions = CompositeDisposable()

        val left = PublishSubject.create<String>()
        val right = PublishSubject.create<String>()

        Observables.combineLatest(left, right) { leftString, rightString ->
            "$leftString $rightString"
        }.subscribe {
            println(it)
        }.addTo(subscriptions)

        left.onNext("Hello")
        right.onNext("World")
        left.onNext("It's nice to")
        right.onNext("be here!")
        left.onNext("Actually, it's super great to")
    }

    exampleOf("zip") {

        val subscriptions = CompositeDisposable()

        val left = PublishSubject.create<String>()
        val right = PublishSubject.create<String>()

        Observables.zip(left, right) { weather, city ->
            "It's $weather in $city"
        }.subscribe {
            println(it)
        }.addTo(subscriptions)

        left.onNext("sunny")
        right.onNext("Lisbon")
        left.onNext("cloudy")
        right.onNext("Copenhagen")
        left.onNext("cloudy")
        right.onNext("London")
        left.onNext("sunny")
        right.onNext("Madrid")
        right.onNext("Vienna")
    }

    exampleOf("withLatestFrom") {
        val subscriptions = CompositeDisposable()

        val button = PublishSubject.create<Unit>()
        val editText = PublishSubject.create<String>()

        button.withLatestFrom(editText) { _: Unit, value: String ->
            value
        }.subscribe {
            println(it)
        }.addTo(subscriptions)

        editText.onNext("Par")
        editText.onNext("Pari")
        editText.onNext("Paris")
        button.onNext(Unit)
        button.onNext(Unit)
    }

    exampleOf("sample") {
        val subscriptions = CompositeDisposable()

        val button = PublishSubject.create<Unit>()
        val editText = PublishSubject.create<String>()

        editText.sample(button)
          .subscribe {
              println(it)
          }.addTo(subscriptions)

        editText.onNext("Par")
        editText.onNext("Pari")
        editText.onNext("Paris")
        button.onNext(Unit)
        button.onNext(Unit)
    }

    exampleOf("amb") {

        val subscriptions = CompositeDisposable()

        val left = PublishSubject.create<String>()
        val right = PublishSubject.create<String>()


        left.ambWith(right)
          .subscribe {
              println(it)
          }
          .addTo(subscriptions)

        left.onNext("Lisbon")
        right.onNext("Copenhagen")
        left.onNext("London")
        left.onNext("Madrid")
        right.onNext("Vienna")
    }

    exampleOf("reduce") {

        val subscriptions = CompositeDisposable()

        val source = Observable.just(1, 3, 5, 7, 9)
        source
          .reduce(0) { a, b -> a + b }
          .subscribeBy(onSuccess = {
              println(it)
          })
          .addTo(subscriptions)
    }

    exampleOf("scan") {

        val subscriptions = CompositeDisposable()

        val source = Observable.just(1, 3, 5, 7, 9)

        source
          .scan(0) { a, b -> a + b }
          .subscribe {
              println(it)
          }
          .addTo(subscriptions)
    }

}

/*
Flowables & Backpressure
*/
fun chapter14() {
    exampleOf("Zipping observable") {
        val fastObservable = Observable.interval(1, TimeUnit.MILLISECONDS)
        val slowObservable = Observable.interval(1, TimeUnit.SECONDS)

        // 1
        val disposable = Observables.zip(slowObservable, fastObservable)
          .subscribeOn(Schedulers.io())
          .subscribe { (first, second) ->
              println("Got $first and $second")
          }
        safeSleep(5000)
        disposable.dispose()
    }

//  exampleOf("Overflowing observer") {
//    val disposable = Observable.range(1, 10_000_000)
//      .subscribeOn(Schedulers.io())
//      .map { LongArray(1024 * 8) }
//      .observeOn(Schedulers.computation())
//      .subscribe {
//        println("$it Free memory: ${freeMemory()}")
//        safeSleep(100)
//      }
//    safeSleep(20_000)
//    disposable.dispose()
//  }

    exampleOf("Zipping flowable") {
        val slowFlowable = Flowable.interval(1, TimeUnit.SECONDS)
        val fastFlowable = Flowable.interval(1, TimeUnit.MILLISECONDS)
          .onBackpressureDrop {
              println("Dropping $it")
          }
        val disposable =
          Flowables.zip(slowFlowable, fastFlowable)
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.newThread())
            .subscribe { (first, second) ->
                println("Got $first and $second")
            }

        safeSleep(5000)
        disposable.dispose()
    }

    exampleOf("onBackPressureBuffer") {
        val disposable = Flowable.range(1, 100)
          .subscribeOn(Schedulers.io())
          .onBackpressureBuffer( 10,
            { println("Buffer overrun; dropping latest") },
            BackpressureOverflowStrategy.DROP_LATEST
          )
          .observeOn(Schedulers.newThread(), false, 1)
          .doOnComplete { println("We're done!") }
          .subscribe {
              println("Integer: $it")
              safeSleep(50)
          }
        safeSleep(1000)
        disposable.dispose()
    }

    exampleOf("onBackPressureLatest") {
        val disposable = Flowable.range(1, 100)
          .subscribeOn(Schedulers.io())
          .onBackpressureLatest()
          .observeOn(Schedulers.newThread(), false, 1)
          .doOnComplete { println("We're done!") }
          .subscribe {
              println("Integer: $it")
              safeSleep(50)
          }
        safeSleep(1000)
        disposable.dispose()
    }

    exampleOf("No backpressure") {
        val disposable = Flowable.range(1, 100)
          .subscribeOn(Schedulers.io())
          .observeOn(Schedulers.newThread(), false, 1)
          .doOnComplete { println("We're done!") }
          .subscribe {
              println("Integer: $it")
              safeSleep(50)
          }
        safeSleep(1000)
        disposable.dispose()
    }

    exampleOf("toFlowable") {
        val disposable = Observable.range(1, 100)
//      .toFlowable(BackpressureStrategy.BUFFER)
          .subscribeOn(Schedulers.io())
          .observeOn(Schedulers.newThread(), false, 1)
          .subscribe {
              println("Integer: $it")
              safeSleep(50)
          }
        safeSleep(1000)
        disposable.dispose()
    }

    exampleOf("Processor") {
        // 1
        val processor = PublishProcessor.create<Int>()
        // 2
        val disposable = processor
          .onBackpressureDrop { println("Dropping $it") }
          .observeOn(Schedulers.newThread(), false, 1)
          .subscribe {
              println("Integer: $it")
              safeSleep(50)
          }
        // 3
        Thread().run {
            for (i in 0..100) {
                processor.onNext(i)
                safeSleep(5)
            }
        }
        safeSleep(1000)
        disposable.dispose()
    }

}
