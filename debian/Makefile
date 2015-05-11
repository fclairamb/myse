build:
	# Done in build.sh

install:
	rsync -av debian/sysfiles/ $(DESTDIR)

	mkdir -p $(DESTDIR)/usr/share/myse $(DESTDIR)/var/lib/myse $(DESTDIR)/var/log/myse
	ln myse/target/*-jar-with-dependencies.jar $(DESTDIR)/usr/share/myse/myse.jar

test-package:
	sudo killall -9 -u myse ; sudo rm -Rf /var/lib/myse /usr/share/myse
	dpkg-buildpackage -us -uc -b && sudo dpkg -i ../myse_*.deb

clean:
	# Done in build.sh
