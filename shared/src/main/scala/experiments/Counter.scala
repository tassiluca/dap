package experiments

trait Counter[T]:
  def ++ : Counter[T] = inc()
  def dec(delta: Int = -1): Counter[T] = inc(delta)
  def -- : Counter[T] = dec()
  def inc(value: Int = 1): Counter[T]
  def value: T

object Counter:
  def ofIntegers(): Counter[Int] = IntImpl()

  private case class IntImpl(value: Int = 0) extends Counter[Int]:
    override def inc(value: Int): Counter[Int] = IntImpl(this.value + value)
