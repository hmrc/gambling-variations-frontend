#!/bin/bash

echo ""
echo "Applying migration CorrespondenceFaxNumber"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /correspondenceFaxNumber                        controllers.CorrespondenceFaxNumberController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /correspondenceFaxNumber                        controllers.CorrespondenceFaxNumberController.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /changeCorrespondenceFaxNumber                  controllers.CorrespondenceFaxNumberController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /changeCorrespondenceFaxNumber                  controllers.CorrespondenceFaxNumberController.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "correspondenceFaxNumber.title = correspondenceFaxNumber" >> ../conf/messages.en
echo "correspondenceFaxNumber.heading = correspondenceFaxNumber" >> ../conf/messages.en
echo "correspondenceFaxNumber.checkYourAnswersLabel = correspondenceFaxNumber" >> ../conf/messages.en
echo "correspondenceFaxNumber.error.required = Enter correspondenceFaxNumber" >> ../conf/messages.en
echo "correspondenceFaxNumber.error.length = CorrespondenceFaxNumber must be 20 characters or less" >> ../conf/messages.en
echo "correspondenceFaxNumber.change.hidden = CorrespondenceFaxNumber" >> ../conf/messages.en

echo "Migration CorrespondenceFaxNumber completed"
