#!/bin/bash

echo ""
echo "Applying migration PremisesAddressList"

echo "Adding routes to conf/app.routes"
echo "" >> ../conf/app.routes
echo "GET        /premisesAddressList                       controllers.PremisesAddressListController.onPageLoad()" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "premisesAddressList.title = premisesAddressList" >> ../conf/messages.en
echo "premisesAddressList.heading = premisesAddressList" >> ../conf/messages.en

echo "Migration PremisesAddressList completed"
