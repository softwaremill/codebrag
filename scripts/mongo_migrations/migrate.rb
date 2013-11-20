#!/usr/bin/env ruby

# Run Mongo migrations using database provided in config file

unless ARGV.length == 2
  puts "Usage: provide arguments in form of [config_file] [path_to_mongo]"
  puts "eg. migrate.rb ../codebrag.conf /opt/mongo/bin/mongo"
  exit
end

def config_value_for(file, key)
  param_line_regexp = Regexp.new("#{key}.*\"(.*)\"")
  line = File.readlines(file).find {|l| l.match(param_line_regexp)}
  param_line_regexp.match(line)[1] if line
end

config_file = ARGV[0]
mongo_path = ARGV[1]

abort "Cannot locate config file #{config_file}" unless File.exists?(config_file)

mongo_server = config_value_for(config_file, 'servers')
mongo_database = config_value_for(config_file, 'database')

abort "Cannot read db parameters from config #{config_file}" unless mongo_server && mongo_database

# running migrations
run_migrations_cmd = "#{mongo_path} #{mongo_server}/#{mongo_database} run_migration.js"
system(run_migrations_cmd, out: $stdout, err: :out)