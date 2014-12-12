clear all;
Infinity = Inf;
cd results

thrp_AC1
thrp_AC2

thrp_SA2_DA1_AC1_ID_60_saturation__End
thrp_SA3_DA1_AC1_ID_90_saturation__End
total_thrp
xthrp1 = result_thrp_AC1(10:end,1);
ythrp1 = result_thrp_AC1(10:end,7);
xthrp2 = result_thrp_AC2(10:end,1);
ythrp2 = result_thrp_AC2(10:end,7);
%xthrp3 = result_thrp_AC3(10:end,1);
%ythrp3 = result_thrp_AC3(10:end,6);
xthrp = result_total_thrp(10:end,1);
ythrp = result_total_thrp(10:end,7);
% 

thrp_sta2 = result_thrp_SA2_DA1_AC1_ID_60_saturation__End(10:end,7)
x_sta2 = result_thrp_SA2_DA1_AC1_ID_60_saturation__End(10:end,1)
thrp_sta3 = result_thrp_SA3_DA1_AC1_ID_90_saturation__End(10:end,7)
y_sta3 = result_thrp_SA3_DA1_AC1_ID_90_saturation__End(10:end,1)

figure
hold on;
plot(xthrp,ythrp,':r')
plot(x_sta2, thrp_sta2, 'b');
plot(y_sta3, thrp_sta3, 'g');
%hold on;
%plot(xthrp2, ythrp2, 'm');
%hold on;
%plot(xthrp3, ythrp3, 'k');

grid on

xlabel('time [ms]')
%ylabel('delay overall average [ms]')
ylabel('thrp [Mb/s]')
legend('total', 'STA2','STA3')
%histrogram(result_thrp_AC1(10:end,9:end));
%sum(ythrp)

result_total_thrp(end,6)

cd ..
