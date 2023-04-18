/*
 * Copyright 2018-2020 Jan Bessai
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.combinators.cls.types

/** Type class for syntactic sugar to construct intersection types. */
trait TypeSyntax {

  /** The type constructed so far. */
  val ty: Type

  /** Intersects `ty` with `other`. */
  def :&:(other: Type): Type =
    Intersection(other, ty)

  /** The product of `ty` and `other` */
  def <*>(other: Type): Type =
    Product(ty, other)

  /** Uses `ty` as the parameter of an arrow resulting in `other`. */
  def =>:(other: Type): Type =
    Arrow(other, ty)
}

/** Instances of `TypeSyntax` for intersection types. */
trait ToTypeSyntax {
  implicit def toTypeSyntax(fromTy: Type): TypeSyntax =
    new TypeSyntax {
      lazy val ty: Type = fromTy
    }
}

/** Type class for syntactic sugar for intersection type constructors. */
trait ConstructorSyntax {

  /** The constructor name. */
  val name: Symbol

  /** Apply `name` to a (non-empty) list of constructor arguments. */
  def apply(arg: Type, args: Type*): Constructor =
    Constructor(
      name.name,
      args.foldLeft(arg)((s, nextArg) => Product(s, nextArg))
    )
}

/** Instances of `ConstructorSyntax` for symbols, so we can use 'A('B) notation. */
trait ToConstructorSyntax extends ToTypeSyntax {

  /** Enables 'A notation for argumentless constructors */
  @deprecated(
    message =
      "consider declaring a variable: 'Foo becomes val Foo = Constructor(\"Foo\")",
    since = "cls-scala 3.0.0"
  )
  implicit def toConstructor(name: Symbol): Constructor =
    Constructor(name.name, Omega)

  /** Enables `ToTypeSyntax` sugar for argumentless constructors . */
  @deprecated(
    message =
      "consider declaring a variable: 'Foo becomes val Foo = Constructor(\"Foo\")",
    since = "cls-scala 3.0.0"
  )
  implicit def toTypeSyntax(name: Symbol): TypeSyntax =
    new TypeSyntax {
      lazy val ty: Type = Constructor(name.name, Omega)
    }

  /** Enables 'A('B) notation for constructors with arguments. */
  @deprecated(
    message =
      "consider declaring a variable: 'Foo(x, y) becomes def Foo(x: Type, y: Type) = Constructor(\"Foo\", x, y)",
    since = "cls-scala 3.0.0"
  )
  implicit def toConstructorSyntax(fromName: Symbol): ConstructorSyntax =
    new ConstructorSyntax {
      lazy val name: Symbol = fromName
    }
}

/** Instances of syntactic sugar for intersection types. */
object syntax extends ToConstructorSyntax
