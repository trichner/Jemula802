
Infinity = Inf;
cd assignment03

thrp_AC1;thrp_AC2;thrp_AC3;
offer_AC1;offer_AC2;offer_AC3;
close all;

plot(result_thrp_AC1(:,1),result_thrp_AC1(:,6),'b-');hold on;
plot(result_thrp_AC2(:,1),result_thrp_AC2(:,6),'k-');
plot(result_thrp_AC3(:,1),result_thrp_AC3(:,6),'m-')

%plot(result_offer_AC1(:,1),result_offer_AC1(:,6),'b.');
%plot(result_offer_AC2(:,1),result_offer_AC2(:,6),'k.');
%plot(result_offer_AC3(:,1),result_offer_AC3(:,6),'m.')

ylim([0,max(ylim)]);

cd ..