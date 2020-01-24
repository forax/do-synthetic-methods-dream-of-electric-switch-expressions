# Dockerfile for java guide
FROM forax/do-synthetic-methods-dream-of-electric-switch-expressions

USER jovyan

# Launch the notebook server
ENV IJAVA_COMPILER_OPTS "--enable-preview --source=15 --add-modules jdk.incubator.foreign"
WORKDIR $HOME/slideshow
CMD ["jupyter", "notebook", "--no-browser", "--ip", "0.0.0.0"]

