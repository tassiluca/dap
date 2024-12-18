package experiments

trait Counter[T]:
  def inc: Counter[T] = inc(1)
  def ++ : Counter[T] = inc
  def dec: Counter[T] = inc(-1)
  def -- : Counter[T] = dec
  def inc(value: Int): Counter[T]
  def value: T

object Counter:
  def apply(): Counter[Int] = IntImpl()

  private case class IntImpl(value: Int = 0) extends Counter[Int]:
    override def inc(value: Int): Counter[Int] = IntImpl(this.value + value)
