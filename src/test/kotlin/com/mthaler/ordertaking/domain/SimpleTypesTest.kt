package com.mthaler.ordertaking.domain

import arrow.core.None
import arrow.core.Some
import arrow.core.Validated
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class SimpleTypesTest: StringSpec({

    "createString50" {
        String50("test", "") shouldBe Validated.Invalid("test must not be empty")
        String50("test", "blah") shouldBe Validated.Valid(String50("blah"))
        String50("test", "a".repeat(60)) shouldBe Validated.Invalid("test must not be more than 50 chars")
    }

    "createOptionString50" {
        String50.createOption("test", "") shouldBe Validated.Valid(None)
        String50.createOption("test", "blah") shouldBe Validated.Valid(Some(String50("blah")))
        String50.createOption("test", "a".repeat(60)) shouldBe Validated.Invalid("test must not be more than 50 chars")
    }

    "createEmailAddress" {
        EmailAddress("test", "foo") shouldBe Validated.Invalid("test: 'foo' must match the pattern '.+@.+'")
        EmailAddress("test", "foo@") shouldBe Validated.Invalid("test: 'foo@' must match the pattern '.+@.+'")
        EmailAddress("test", "@bar") shouldBe Validated.Invalid("test: '@bar' must match the pattern '.+@.+'")
        EmailAddress("test", "foo@bar") shouldBe Validated.Valid(EmailAddress("foo@bar"))
    }

    "createZipCode" {
        ZipCode("test", "foo") shouldBe Validated.Invalid("test: 'foo' must match the pattern '\\d{5}'")
        ZipCode("test", "1234") shouldBe Validated.Invalid("test: '1234' must match the pattern '\\d{5}'")
        ZipCode("test", "12345") shouldBe Validated.Valid(ZipCode("12345"))
    }
})