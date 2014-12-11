clear all;
Infinity = Inf;
cd results

thrp_AC1
%thrp_AC2
%thrp_AC3
total_thrp
xthrp1 = result_thrp_AC1(10:end,1);
ythrp1 = result_thrp_AC1(10:end,6);
%xthrp2 = result_thrp_AC2(10:end,1);
%ythrp2 = result_thrp_AC2(10:end,6);
%xthrp3 = result_thrp_AC3(10:end,1);
%ythrp3 = result_thrp_AC3(10:end,6);
xthrp = result_total_thrp(10:end,1);
ythrp = result_total_thrp(10:end,6);
% 
plot(xthrp, ythrp, 'b');
hold on;
plot(xthrp1, ythrp1, 'g');
%hold on;
%plot(xthrp2, ythrp2, 'm');
%hold on;
%plot(xthrp3, ythrp3, 'k');

xlabel('time [ms]')
%ylabel('delay overall average [ms]')
ylabel('thrp overall[Mb/s]')
legend('total thrp', 'AC1','AC2','AC3')
%histrogram(result_thrp_AC1(10:end,9:end));
sum(ythrp)

cd ..
