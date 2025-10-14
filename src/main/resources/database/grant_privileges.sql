USE `ah_device_qos_evaluator`;

REVOKE ALL, GRANT OPTION FROM 'deviceqosevaluator'@'localhost';

GRANT ALL PRIVILEGES ON `ah_device_qos_evaluator`.`logs` TO 'deviceqosevaluator'@'localhost';
GRANT ALL PRIVILEGES ON `ah_device_qos_evaluator`.`device` TO 'deviceqosevaluator'@'localhost';
GRANT ALL PRIVILEGES ON `ah_device_qos_evaluator`.`system_` TO 'deviceqosevaluator'@'localhost';

REVOKE ALL, GRANT OPTION FROM 'deviceqosevaluator'@'%';

GRANT ALL PRIVILEGES ON `ah_device_qos_evaluator`.`logs` TO 'deviceqosevaluator'@'%';
GRANT ALL PRIVILEGES ON `ah_device_qos_evaluator`.`device` TO 'deviceqosevaluator'@'%';
GRANT ALL PRIVILEGES ON `ah_device_qos_evaluator`.`system_` TO 'deviceqosevaluator'@'%';

FLUSH PRIVILEGES;