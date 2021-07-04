package com.mthaler.ordertaking.domain

import arrow.core.None
import arrow.core.Some
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import com.mthaler.ordertaking.Result

class SimpleTypesTest: StringSpec({

    "createString50" {
        String50("test", "") shouldBe Result.Error("test must not be empty")
        String50("test", "blah") shouldBe Result.Ok(String50("blah"))
        String50("test", "a".repeat(60)) shouldBe Result.Error("test must not be more than 50 chars")
    }

    "createOptionString50" {
        String50.createOption("test", "") shouldBe Result.Ok(None)
        String50.createOption("test", "blah") shouldBe Result.Ok(Some(String50("blah")))
        String50.createOption("test", "a".repeat(60)) shouldBe Result.Error("test must not be more than 50 chars")
    }

    "createEmailAddress" {
        EmailAddress("test", "foo") shouldBe Result.Error("test: 'foo' must match the pattern '.+@.+'")
        EmailAddress("test", "foo@") shouldBe Result.Error("test: 'foo@' must match the pattern '.+@.+'")
        EmailAddress("test", "@bar") shouldBe Result.Error("test: '@bar' must match the pattern '.+@.+'")
        EmailAddress("test", "foo@bar") shouldBe Result.Ok(EmailAddress("foo@bar"))
    }

    "createZipCode" {
        ZipCode("test", "foo") shouldBe Result.Error("test: 'foo' must match the pattern '\\d{5}'")
        ZipCode("test", "1234") shouldBe Result.Error("test: '1234' must match the pattern '\\d{5}'")
        ZipCode("test", "12345") shouldBe Result.Ok(ZipCode("12345"))
    }
})