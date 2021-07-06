package com.mthaler.ordertaking.domain

import arrow.core.None
import arrow.core.Some
import arrow.core.Validated
import arrow.core.invalidNel
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class SimpleTypesTest: StringSpec({

    "createString50" {
        String50("test", "") shouldBe "test must not be empty".invalidNel()
        String50("test", "blah") shouldBe Validated.Valid(String50("blah"))
        String50("test", "a".repeat(60)) shouldBe "test must not be more than 50 chars".invalidNel()
    }

    "createOptionString50" {
        String50.createOption("test", "") shouldBe Validated.Valid(None)
        String50.createOption("test", "blah") shouldBe Validated.Valid(Some(String50("blah")))
        String50.createOption("test", "a".repeat(60)) shouldBe "test must not be more than 50 chars".invalidNel()
    }

    "createEmailAddress" {
        EmailAddress("test", "foo") shouldBe "test: 'foo' must match the pattern '.+@.+'".invalidNel()
        EmailAddress("test", "foo@") shouldBe "test: 'foo@' must match the pattern '.+@.+'".invalidNel()
        EmailAddress("test", "@bar") shouldBe "test: '@bar' must match the pattern '.+@.+'".invalidNel()
        EmailAddress("test", "foo@bar") shouldBe Validated.Valid(EmailAddress("foo@bar"))
    }

    "createZipCode" {
        ZipCode("test", "foo") shouldBe "test: 'foo' must match the pattern '\\d{5}'".invalidNel()
        ZipCode("test", "1234") shouldBe "test: '1234' must match the pattern '\\d{5}'".invalidNel()
        ZipCode("test", "12345") shouldBe Validated.Valid(ZipCode("12345"))
    }
})