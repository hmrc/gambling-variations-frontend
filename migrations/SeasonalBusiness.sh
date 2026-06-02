#!/bin/bash

echo ""
echo "Applying migration SeasonalBusiness"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /seasonalBusiness                        controllers.SeasonalBusinessController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /seasonalBusiness                        controllers.SeasonalBusinessController.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /changeSeasonalBusiness                  controllers.SeasonalBusinessController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /changeSeasonalBusiness                  controllers.SeasonalBusinessController.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "seasonalBusiness.title = seasonalBusiness" >> ../conf/messages.en
echo "seasonalBusiness.heading = seasonalBusiness" >> ../conf/messages.en
echo "seasonalBusiness.checkYourAnswersLabel = seasonalBusiness" >> ../conf/messages.en
echo "seasonalBusiness.error.required = Select yes if seasonalBusiness" >> ../conf/messages.en
echo "seasonalBusiness.change.hidden = SeasonalBusiness" >> ../conf/messages.en

echo "Migration SeasonalBusiness completed"
