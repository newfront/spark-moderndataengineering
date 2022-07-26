use default;

CREATE TABLE IF NOT EXISTS `bettercustomers` (
  `id` mediumint NOT NULL AUTO_INCREMENT COMMENT 'customer automatic id',
  `created` timestamp DEFAULT CURRENT_TIMESTAMP COMMENT 'customer join date',
  `updated` timestamp DEFAULT CURRENT_TIMESTAMP COMMENT 'last record update',
  `first_name` varchar(100) NOT NULL,
  `last_name` varchar(100) NOT NULL,
  `email` varchar(255) NOT NULL UNIQUE,
  PRIMARY KEY (`id`)
);

/* Note: If you would like to retain the customer records - only run this bootstrap script once*/
TRUNCATE TABLE `bettercustomers`;

INSERT INTO bettercustomers (created, first_name, last_name, email)
VALUES
("2021-02-16 00:16:06", "Scott", "Haines", "scott@coffeeco.com"),
("2021-02-16 00:16:06", "John", "Hamm", "john.hamm@acme.com"),
("2021-02-16 00:16:06", "Milo", "Haines", "mhaines@coffeeco.com"),
("2021-02-21 21:00:00", "Penny", "Haines", "penny@coffeeco.com"),
("2021-02-21 22:00:00", "Cloud", "Fast", "cloud.fast@acme.com"),
("2021-02-21 23:00:00", "Marshal", "Haines", "paws@coffeeco.com"),
("2021-02-24 09:00:00", "Willow", "Haines", "willow@coffeeco.com"),
("2021-02-24 09:00:00", "Clover", "Haines", "pup@coffeeco.com");
