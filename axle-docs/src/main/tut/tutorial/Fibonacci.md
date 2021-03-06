---
layout: page
title: Fibonacci
permalink: /tutorial/fibonacci/
---


```tut:silent
import axle.math._
```

## Linear using `foldLeft`

```tut:book
fibonacciByFold(10)
```

## Recursive

```tut:book
fibonacciRecursively(10)
```

Some alternatives that are not in Axle include

## Recursive with memoization

```tut:book
val memo = collection.mutable.Map(0 -> 0L, 1 -> 1L)

def fibonacciRecursivelyWithMemo(n: Int): Long = {
  if (memo.contains(n)) {
    memo(n)
  } else {
    val result = fibonacciRecursivelyWithMemo(n - 2) + fibonacciRecursivelyWithMemo(n - 1)
    memo += n -> result
    result
  }
}

fibonacciRecursivelyWithMemo(10)
```

## Recursive squaring

Imports

```tut:silent
import axle._
import axle.jblas._
import spire.implicits._
import org.jblas.DoubleMatrix

implicit val laJblasDouble = axle.jblas.linearAlgebraDoubleMatrix[Double]
import laJblasDouble._
```

The fibonacci sequence at N can be generated by taking the Nth power of a special 2x2 matrix.
By employing the general-purpose strategy for exponentiation called "recursive squaring",
we can achieve sub-linear time.

```tut:book
val base = fromColumnMajorArray(2, 2, List(1d, 1d, 1d, 0d).toArray)

def fibonacciSubLinear(n: Int): Long = n match {
  case 0 => 0L
  case _ => exponentiateByRecursiveSquaring(base, n).get(0, 1).toLong
}
```

Demo:

```tut:book
fibonacciSubLinear(78)
```

Note: Beyond 78 inaccuracies creep in due to the limitations of the `Double` number type.
