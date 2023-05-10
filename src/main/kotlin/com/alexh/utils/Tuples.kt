package com.alexh.utils

data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
data class Quint<A, B, C, D, E>(val first: A, val second: B, val third: C, val fourth: D, val fifth: E)

infix fun <A, B, C> Pair<A, B>.to(that: C): Triple<A, B, C> =
    Triple(this.first, this.second, that)
infix fun <A, B, C> A.to(pair: Pair<B, C>): Triple<A, B, C> =
    Triple(this, pair.first, pair.second)
infix fun <A, B, C, D> Triple<A, B, C>.to(that: D): Quad<A, B, C, D> =
    Quad(this.first, this.second, this.third, that)
infix fun <A, B, C, D> A.to(triple: Triple<B, C, D>): Quad<A, B, C, D> =
    Quad(this, triple.first, triple.second, triple.third)
infix fun <A, B, C, D> Pair<A, B>.to(pair: Pair<C, D>): Quad<A, B, C, D> =
    Quad(this.first, this.second, pair.first, pair.second)
infix fun <A, B, C, D, E> Quad<A, B, C, D>.to(that: E): Quint<A, B, C, D, E> =
    Quint(this.first, this.second, this.third, this.fourth, that)
infix fun <A, B, C, D, E> A.to(quad: Quad<B, C, D, E>): Quint<A, B, C, D, E> =
    Quint(this, quad.first, quad.second, quad.third, quad.fourth)
infix fun <A, B, C, D, E> Triple<A, B, C>.to(pair: Pair<D, E>): Quint<A, B, C, D, E> =
    Quint(this.first, this.second, this.third, pair.first, pair.second)
infix fun <A, B, C, D, E> Pair<A, B>.to(triple: Triple<C, D, E>): Quint<A, B, C, D, E> =
    Quint(this.first, this.second, triple.first, triple.second, triple.third)
