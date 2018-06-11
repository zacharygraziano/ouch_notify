# ouch notify

an AWS Lambda function that responds to gifs being placed in an s3 bucket and notifies
recipients in a Cognito User Pool via SMS. such gifs may come from any source, such a meme
generator or a very low to the ground chandelier that people hit their heads on frequently.

if you want to run this, you'll need an `application.conf` file like this:

```
s3Bucket = # s3 bucket to look in
userPoolId = # userpool to lookup phone numbers from
outgoingPhone = # phone number to send from

messages = [
    # text of messages, a different one is randomly chosen each time the function
    # is invoked
]
```

# license
this code is copyright 2018 zachary graziano and licensed under the MIT license. see
`LICENSE`