# consent-management-api
This package defines service code for the Consent Management API.

See [API models here](https://github.com/msayson/consent-management-api-models/).

See [v1 API documentation here](https://www.marksayson.com/consent-management-api-models/v1/docs.html).

## Technologies
[Smithy](https://smithy.io) is used to produce protocol and technology agnostic API models that can be used to automatically generate:
* API specifications for various types of platforms
* API clients for various programming languages

[ReDoc](https://github.com/Redocly/redoc) is used to automatically generate API documentation from our API models.

[OpenAPI Generator](https://openapi-generator.tech) is used to automatically generate Java data models and client code from our Smithy-generated OpenAPI spec.

The [AWS SDK for Java 2.x](https://docs.aws.amazon.com/sdk-for-java/latest/developer-guide) is used to write Java application code integrating with AWS services such as Lambda and DynamoDB.

[GitHub Actions](https://docs.github.com/en/actions) are used to automatically generate and deploy HTML API documentation to [GitHub Pages](https://pages.github.com/) after changes are pushed to the main branch.

[Gradle](https://docs.gradle.org) is used to build the project and manage package dependencies.

## License
The code in this project is released under the [GPL-3.0 License](LICENSE).
