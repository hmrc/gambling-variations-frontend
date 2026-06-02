#!/bin/bash

echo ""
echo "Applying migration AssociatedRegistrationNumbers"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /associatedRegistrationNumbers                        controllers.AssociatedRegistrationNumbersController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /associatedRegistrationNumbers                        controllers.AssociatedRegistrationNumbersController.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /changeAssociatedRegistrationNumbers                  controllers.AssociatedRegistrationNumbersController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /changeAssociatedRegistrationNumbers                  controllers.AssociatedRegistrationNumbersController.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "associatedRegistrationNumbers.title = associatedRegistrationNumbers" >> ../conf/messages.en
echo "associatedRegistrationNumbers.heading = associatedRegistrationNumbers" >> ../conf/messages.en
echo "associatedRegistrationNumbers.checkYourAnswersLabel = associatedRegistrationNumbers" >> ../conf/messages.en
echo "associatedRegistrationNumbers.error.required = Select yes if associatedRegistrationNumbers" >> ../conf/messages.en
echo "associatedRegistrationNumbers.change.hidden = AssociatedRegistrationNumbers" >> ../conf/messages.en

echo "Migration AssociatedRegistrationNumbers completed"
