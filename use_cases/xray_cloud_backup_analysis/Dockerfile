FROM ruby:2.7.8

# throw errors if Gemfile has been modified since Gemfile.lock
RUN bundle config --global frozen 1

WORKDIR /usr/src/app

COPY Gemfile Gemfile.lock process_xray_cloud_backup.rb ./
RUN bundle install

ENTRYPOINT ["/usr/local/bin/bundle","exec","ruby","./process_xray_cloud_backup.rb"]
#CMD ["exec","ruby","./process_xray_cloud_backup.rb"]